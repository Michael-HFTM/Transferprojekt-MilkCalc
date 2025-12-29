package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.javafx.dialogs.AssignmentDialog;
import com.example.transferprojekt.services.AssignmentService;
import com.example.transferprojekt.services.SupplierService;
import com.example.transferprojekt.services.SupplierNrService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AssignmentView extends BorderPane {

    private final AssignmentService assignmentService;
    private final SupplierService supplierService;
    private final SupplierNrService supplierNrService;

    // UI Components
    private TableView<Assignment> tableView;
    private TextField searchField;
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button refreshButton;

    // Data
    private ObservableList<Assignment> assignmentList;

    public AssignmentView(AssignmentService assignmentService,
                          SupplierService supplierService,
                          SupplierNrService supplierNrService) {
        this.assignmentService = assignmentService;
        this.supplierService = supplierService;
        this.supplierNrService = supplierNrService;
        this.assignmentList = FXCollections.observableArrayList();

        initializeUI();
        loadAssignments();
    }

    private void initializeUI() {
        // Top: Toolbar with buttons
        HBox toolbar = createToolbar();

        // Center: TableView
        tableView = createTableView();

        // Layout
        VBox centerContent = new VBox(10);
        centerContent.setPadding(new Insets(10));
        centerContent.getChildren().addAll(toolbar, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        setCenter(centerContent);
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5));

        // Search Field
        searchField = new TextField();
        searchField.setPromptText("Suche nach Lieferant, Lieferantennummer, Status...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAssignments(newVal));

        // Buttons
        addButton = new Button("Hinzufügen");
        addButton.setOnAction(e -> addAssignment());

        editButton = new Button("Bearbeiten");
        editButton.setOnAction(e -> editAssignment());
        editButton.setDisable(true);

        deleteButton = new Button("Löschen");
        deleteButton.setOnAction(e -> deleteAssignment());
        deleteButton.setDisable(true);

        refreshButton = new Button("Aktualisieren");
        refreshButton.setOnAction(e -> loadAssignments());

        // Spacer
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(
                searchField,
                addButton,
                editButton,
                deleteButton,
                spacer,
                refreshButton
        );

        return toolbar;
    }

    private TableView<Assignment> createTableView() {
        TableView<Assignment> table = new TableView<>();
        table.setItems(assignmentList);

        // Column: Assignment ID (hidden)
        TableColumn<Assignment, UUID> assignmentIdColumn = new TableColumn<>("Zuweisungs-ID");
        assignmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("assignmentId"));
        assignmentIdColumn.setVisible(false);

        // Column: Supplier ID (hidden, for lookup)
        TableColumn<Assignment, UUID> supplierIdColumn = new TableColumn<>("Lieferanten-ID");
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supplierIdColumn.setVisible(false);

        // Column: Supplier Name (derived from supplierId)
        TableColumn<Assignment, String> supplierNameColumn = new TableColumn<>("Lieferant");
        supplierNameColumn.setCellValueFactory(cellData -> {
            UUID supplierId = cellData.getValue().getSupplierId();
            try {
                var supplier = supplierService.getById(supplierId);
                if (supplier != null) {
                    return new javafx.beans.property.SimpleStringProperty(supplier.getName());
                }
            } catch (Exception e) {
                // Handle error silently
            }
            return new javafx.beans.property.SimpleStringProperty("Unbekannt");
        });
        supplierNameColumn.setPrefWidth(200);

        // Column: Supplier Number
        TableColumn<Assignment, String> supplierNumberColumn = new TableColumn<>("Lieferantennummer");
        supplierNumberColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(cellData.getValue().getSupplierNumber().getId())
                )
        );
        supplierNumberColumn.setPrefWidth(150);

        // Column: Valid From
        TableColumn<Assignment, LocalDate> validFromColumn = new TableColumn<>("Gültig ab");
        validFromColumn.setCellValueFactory(new PropertyValueFactory<>("validFrom"));
        validFromColumn.setPrefWidth(120);

        // Column: Valid To
        TableColumn<Assignment, LocalDate> validToColumn = new TableColumn<>("Gültig bis");
        validToColumn.setCellValueFactory(new PropertyValueFactory<>("validTo"));
        validToColumn.setPrefWidth(120);

        // Column: Status (active/inactive)
        TableColumn<Assignment, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> {
            LocalDate validFrom = cellData.getValue().getValidFrom();
            LocalDate validTo = cellData.getValue().getValidTo();
            LocalDate now = LocalDate.now();

            String status;
            if (validFrom.isAfter(now)) {
                status = "Zukünftig";
            } else if (validTo != null && validTo.isBefore(now)) {
                status = "Abgelaufen";
            } else {
                status = "Aktiv";
            }

            return new javafx.beans.property.SimpleStringProperty(status);
        });
        statusColumn.setPrefWidth(100);

        table.getColumns().addAll(
                supplierNameColumn,
                supplierNumberColumn,
                validFromColumn,
                validToColumn,
                statusColumn
        );

        // Enable/disable buttons based on selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        // Double-click to edit
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                editAssignment();
            }
        });

        return table;
    }

    /**
     * Loads all assignments from database
     */
    private void loadAssignments() {
        try {
            List<Assignment> assignments = assignmentService.getDatabaseEntries();
            assignmentList.clear();
            assignmentList.addAll(assignments);

            // Clear search field
            searchField.clear();

        } catch (Exception e) {
            showErrorDialog("Fehler beim Laden", "Zuweisungen konnten nicht geladen werden.", e.getMessage());
        }
    }

    /**
     * Filters assignments based on search text
     */
    private void filterAssignments(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadAssignments();
            return;
        }

        try {
            List<Assignment> allAssignments = assignmentService.getDatabaseEntries();
            String lowerCaseFilter = searchText.toLowerCase();

            List<Assignment> filtered = allAssignments.stream()
                    .filter(assignment -> {
                        // Search in supplier number
                        boolean matchesSupplierNumber = String.valueOf(assignment.getSupplierNumber().getId())
                                .contains(lowerCaseFilter);

                        // Search in supplier name
                        boolean matchesSupplierName = false;
                        try {
                            var supplier = supplierService.getById(assignment.getSupplierId());
                            if (supplier != null) {
                                matchesSupplierName = supplier.getName().toLowerCase().contains(lowerCaseFilter);
                            }
                        } catch (Exception e) {
                            // Ignore lookup errors
                        }

                        // Search in status
                        boolean matchesStatus = false;
                        LocalDate validFrom = assignment.getValidFrom();
                        LocalDate validTo = assignment.getValidTo();
                        LocalDate now = LocalDate.now();

                        String status;
                        if (validFrom.isAfter(now)) {
                            status = "zukünftig";
                        } else if (validTo != null && validTo.isBefore(now)) {
                            status = "abgelaufen";
                        } else {
                            status = "aktiv";
                        }
                        matchesStatus = status.contains(lowerCaseFilter);

                        // Return true if any field matches
                        return matchesSupplierNumber || matchesSupplierName || matchesStatus;
                    })
                    .toList();

            assignmentList.clear();
            assignmentList.addAll(filtered);

        } catch (Exception e) {
            showErrorDialog("Fehler bei der Suche", "Suche konnte nicht durchgeführt werden.", e.getMessage());
        }
    }

    /**
     * Opens dialog to add a new assignment
     */
    private void addAssignment() {
        AssignmentDialog.showAddDialog(supplierService, supplierNrService).ifPresent(newAssignment -> {
            try {
                assignmentService.save(newAssignment);
                showInfoDialog("Erfolg", "Zuweisung wurde erfolgreich hinzugefügt.");
                loadAssignments();

            } catch (Exception e) {
                showErrorDialog("Fehler beim Hinzufügen",
                        "Zuweisung konnte nicht gespeichert werden.",
                        e.getMessage());
            }
        });
    }

    /**
     * Opens dialog to edit selected assignment
     */
    private void editAssignment() {
        Assignment selectedAssignment = tableView.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            return;
        }

        AssignmentDialog.showEditDialog(selectedAssignment, supplierService, supplierNrService)
                .ifPresent(updatedAssignment -> {
                    try {
                        assignmentService.save(updatedAssignment);
                        showInfoDialog("Erfolg", "Zuweisung wurde erfolgreich aktualisiert.");
                        loadAssignments();

                    } catch (Exception e) {
                        showErrorDialog("Fehler beim Speichern",
                                "Änderungen konnten nicht gespeichert werden.",
                                e.getMessage());
                    }
                });
    }

    /**
     * Deletes selected assignment after confirmation
     */
    private void deleteAssignment() {
        Assignment selectedAssignment = tableView.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            return;
        }

        // Get supplier name for confirmation
        String supplierName = "Unbekannt";
        try {
            var supplier = supplierService.getById(selectedAssignment.getSupplierId());
            if (supplier != null) {
                supplierName = supplier.getName();
            }
        } catch (Exception e) {
            // Use "Unbekannt" as fallback
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Löschen bestätigen");
        confirmation.setHeaderText("Zuweisung wirklich löschen?");
        confirmation.setContentText(
                "Lieferant: " + supplierName + "\n" +
                        "Lieferantennummer: " + selectedAssignment.getSupplierNumber().getId() + "\n" +
                        "Gültig ab: " + selectedAssignment.getValidFrom() + "\n\n" +
                        "Diese Aktion kann nicht rückgängig gemacht werden!"
        );

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = assignmentService.deleteById(selectedAssignment.getAssignmentId());

                    if (success) {
                        showInfoDialog("Erfolg", "Zuweisung wurde gelöscht.");
                        loadAssignments();
                    } else {
                        showErrorDialog("Fehler", "Zuweisung konnte nicht gelöscht werden.",
                                "Möglicherweise existieren noch Abhängigkeiten.");
                    }

                } catch (Exception e) {
                    showErrorDialog("Fehler beim Löschen", "Ein Fehler ist aufgetreten.", e.getMessage());
                }
            }
        });
    }

    /**
     * Shows an error dialog
     */
    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows an info dialog
     */
    private void showInfoDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}