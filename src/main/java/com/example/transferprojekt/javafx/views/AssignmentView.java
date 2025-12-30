package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.javafx.dialogs.AssignmentDialog;
import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.javafx.utils.DialogUtils;
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
    private List<Assignment> allAssignments; // Cache for local filtering

    public AssignmentView(AssignmentService assignmentService,
                          SupplierService supplierService,
                          SupplierNrService supplierNrService) {
        this.assignmentService = assignmentService;
        this.supplierService = supplierService;
        this.supplierNrService = supplierNrService;
        this.assignmentList = FXCollections.observableArrayList();
        this.allAssignments = new java.util.ArrayList<>();

        initializeUI();
        loadAssignments();
    }

    private void initializeUI() {
        HBox toolbar = createToolbar();
        tableView = createTableView();

        VBox centerContent = new VBox(10);
        centerContent.setPadding(new Insets(10));
        centerContent.getChildren().addAll(toolbar, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        setCenter(centerContent);
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5));

        searchField = new TextField();
        searchField.setPromptText("Suche nach Lieferant, Lieferantennummer, Status...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAssignments(newVal));

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

        TableColumn<Assignment, UUID> assignmentIdColumn = new TableColumn<>("Zuweisungs-ID");
        assignmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("assignmentId"));
        assignmentIdColumn.setVisible(false);

        TableColumn<Assignment, UUID> supplierIdColumn = new TableColumn<>("Lieferanten-ID");
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supplierIdColumn.setVisible(false);

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

        TableColumn<Assignment, String> supplierNumberColumn = new TableColumn<>("Lieferantennummer");
        supplierNumberColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(cellData.getValue().getSupplierNumber().getId())
                )
        );
        supplierNumberColumn.setPrefWidth(150);

        TableColumn<Assignment, LocalDate> validFromColumn = new TableColumn<>("Gültig ab");
        validFromColumn.setCellValueFactory(new PropertyValueFactory<>("validFrom"));
        validFromColumn.setPrefWidth(120);

        TableColumn<Assignment, LocalDate> validToColumn = new TableColumn<>("Gültig bis");
        validToColumn.setCellValueFactory(new PropertyValueFactory<>("validTo"));
        validToColumn.setPrefWidth(120);

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

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                editAssignment();
            }
        });

        return table;
    }

    /**
     * Loads all assignments from database (ASYNC)
     */
    private void loadAssignments() {
        setButtonsEnabled(false);

        AsyncDatabaseTask.run(
                assignmentService::getDatabaseEntries,
                this,
                assignments -> {
                    allAssignments = assignments;
                    assignmentList.clear();
                    assignmentList.addAll(assignments);
                    searchField.clear();
                    setButtonsEnabled(true);
                },
                error -> {
                    DialogUtils.showError("Fehler beim Laden", "Zuweisungen konnten nicht geladen werden.\n" + error.getMessage());
                    setButtonsEnabled(true);
                }
        );
    }

    /**
     * Filters assignments based on search text (LOCAL)
     */
    private void filterAssignments(String searchText) {
        if (allAssignments == null) {
            return;
        }

        if (searchText == null || searchText.trim().isEmpty()) {
            assignmentList.clear();
            assignmentList.addAll(allAssignments);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();

        List<Assignment> filtered = allAssignments.stream()
                .filter(assignment -> {
                    boolean matchesSupplierNumber = String.valueOf(assignment.getSupplierNumber().getId())
                            .contains(lowerCaseFilter);

                    boolean matchesSupplierName = false;
                    try {
                        var supplier = supplierService.getById(assignment.getSupplierId());
                        if (supplier != null) {
                            matchesSupplierName = supplier.getName().toLowerCase().contains(lowerCaseFilter);
                        }
                    } catch (Exception e) {
                        // Ignore lookup errors
                    }

                    boolean matchesStatus;
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

                    return matchesSupplierNumber || matchesSupplierName || matchesStatus;
                })
                .toList();

        assignmentList.clear();
        assignmentList.addAll(filtered);
    }

    /**
     * Opens dialog to add a new assignment (ASYNC)
     */
    private void addAssignment() {
        AssignmentDialog.showAddDialog(supplierService, supplierNrService).ifPresent(newAssignment -> {
            setButtonsEnabled(false);

            AsyncDatabaseTask.runVoid(
                    () -> assignmentService.save(newAssignment),
                    this,
                    () -> {
                        DialogUtils.showInfo("Erfolg", "Zuweisung wurde erfolgreich hinzugefügt.");
                        loadAssignments();
                    },
                    error -> {
                        DialogUtils.showError("Fehler beim Hinzufügen",
                                "Zuweisung konnte nicht gespeichert werden.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        });
    }

    /**
     * Opens dialog to edit selected assignment (ASYNC)
     */
    private void editAssignment() {
        Assignment selectedAssignment = tableView.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            return;
        }

        AssignmentDialog.showEditDialog(selectedAssignment, supplierService, supplierNrService)
                .ifPresent(updatedAssignment -> {
                    setButtonsEnabled(false);

                    AsyncDatabaseTask.runVoid(
                            () -> assignmentService.save(updatedAssignment),
                            this,
                            () -> {
                                DialogUtils.showInfo("Erfolg", "Zuweisung wurde erfolgreich aktualisiert.");
                                loadAssignments();
                            },
                            error -> {
                                DialogUtils.showError("Fehler beim Speichern",
                                        "Änderungen konnten nicht gespeichert werden.\n" + error.getMessage());
                                setButtonsEnabled(true);
                            }
                    );
                });
    }

    /**
     * Deletes selected assignment after confirmation (ASYNC)
     */
    private void deleteAssignment() {
        Assignment selectedAssignment = tableView.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            return;
        }

        String supplierName = "Unbekannt";
        try {
            var supplier = supplierService.getById(selectedAssignment.getSupplierId());
            if (supplier != null) {
                supplierName = supplier.getName();
            }
        } catch (Exception e) {
            // Use "Unbekannt" as fallback
        }

        String itemDescription = "Lieferant: " + supplierName + "\n" +
                "Lieferantennummer: " + selectedAssignment.getSupplierNumber().getId() + "\n" +
                "Gültig ab: " + selectedAssignment.getValidFrom();

        if (DialogUtils.showDeleteConfirmation(itemDescription)) {
            setButtonsEnabled(false);

            AsyncDatabaseTask.run(
                    () -> assignmentService.deleteById(selectedAssignment.getAssignmentId()),
                    this,
                    success -> {
                        if (success) {
                            DialogUtils.showInfo("Erfolg", "Zuweisung wurde gelöscht.");
                            loadAssignments();
                        } else {
                            DialogUtils.showError("Fehler", "Zuweisung konnte nicht gelöscht werden.\n" +
                                    "Möglicherweise existieren noch Abhängigkeiten.");
                            setButtonsEnabled(true);
                        }
                    },
                    error -> {
                        DialogUtils.showError("Fehler beim Löschen", "Ein Fehler ist aufgetreten.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        addButton.setDisable(!enabled);
        refreshButton.setDisable(!enabled);

        if (enabled) {
            boolean hasSelection = tableView.getSelectionModel().getSelectedItem() != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        } else {
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }
}