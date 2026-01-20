package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.javafx.dialogs.SupplierDialog;
import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.javafx.utils.DialogUtils;
import com.example.transferprojekt.services.SupplierService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;

/**
 * View for managing suppliers (CRUD operations)
 * Now with asynchronous database operations to prevent GUI freezing
 */
public class SupplierView extends BorderPane {

    private final SupplierService supplierService;

    // UI Components
    private TableView<Company> tableView;
    private TextField searchField;
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button refreshButton;

    // Data
    private ObservableList<Company> supplierList;
    private List<Company> allSuppliers; // Cache for local filtering

    public SupplierView(SupplierService supplierService) {
        this.supplierService = supplierService;
        this.supplierList = FXCollections.observableArrayList();
        this.allSuppliers = new java.util.ArrayList<>();

        initializeUI();
        loadSuppliers();
    }

    private void initializeUI() {
        // Top: Toolbar mit Buttons
        HBox toolbar = createToolbar();

        // Center: TableView
        tableView = createTableView();

        // Layout zusammenbauen
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
        searchField.setPromptText("Suche nach Name, Email, Ort...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterSuppliers(newVal));

        // Buttons
        addButton = new Button("Hinzufügen");
        addButton.setOnAction(e -> addSupplier());

        editButton = new Button("Bearbeiten");
        editButton.setOnAction(e -> editSupplier());
        editButton.setDisable(true); // Initially disabled

        deleteButton = new Button("Löschen");
        deleteButton.setOnAction(e -> deleteSupplier());
        deleteButton.setDisable(true); // Initially disabled

        refreshButton = new Button("Aktualisieren");
        refreshButton.setOnAction(e -> loadSuppliers());

        // Spacer to push refresh button to the right
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

    private TableView<Company> createTableView() {
        TableView<Company> table = new TableView<>();
        table.setItems(supplierList);

        // Column: UUID
        TableColumn<Company, UUID> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("companyId"));
        idColumn.setPrefWidth(80);
        idColumn.setVisible(false); // Hidden by default, useful for debugging

        // Column: Name
        TableColumn<Company, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAddress().getName()
                )
        );
        nameColumn.setPrefWidth(200);

        // Column: Street
        TableColumn<Company, String> streetColumn = new TableColumn<>("Strasse");
        streetColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAddress().getStreet()
                )
        );
        streetColumn.setPrefWidth(180);

        // Column: ZIP
        TableColumn<Company, String> zipColumn = new TableColumn<>("PLZ");
        zipColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAddress().getZip()
                )
        );
        zipColumn.setPrefWidth(80);

        // Column: City
        TableColumn<Company, String> cityColumn = new TableColumn<>("Ort");
        cityColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAddress().getCity()
                )
        );
        cityColumn.setPrefWidth(150);

        // Column: Email
        TableColumn<Company, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("mail"));
        emailColumn.setPrefWidth(200);

        table.getColumns().addAll(nameColumn, streetColumn, zipColumn, cityColumn, emailColumn);

        // Enable/disable edit and delete buttons based on selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        table.setRowFactory(tv -> {
            TableRow<Company> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editSupplier();
                }
            });
            return row;
        });

        return table;
    }

    /**
     * Loads all suppliers from database (ASYNC)
     */
    private void loadSuppliers() {
        // Disable buttons during loading
        setButtonsEnabled(false);

        // Database operation in background thread
        AsyncDatabaseTask.run(
                supplierService::getDatabaseEntries,
                this,  // StackPane for loading overlay
                suppliers -> {
                    // Success callback (on JavaFX thread)
                    allSuppliers = suppliers; // Cache all suppliers
                    supplierList.clear();
                    supplierList.addAll(suppliers);

                    // Clear search field
                    searchField.clear();

                    // Re-enable buttons
                    setButtonsEnabled(true);
                },
                error -> {
                    // Error callback (on JavaFX thread)
                    DialogUtils.showError("Fehler beim Laden",
                            "Lieferanten konnten nicht geladen werden.\n" + error.getMessage());

                    // Re-enable buttons even on error
                    setButtonsEnabled(true);
                }
        );
    }

    /**
     * Filters suppliers based on search text (LOCAL - no database call)
     */
    private void filterSuppliers(String searchText) {
        // Guard: No data loaded yet
        if (allSuppliers == null) {
            return;
        }

        // Empty search: show all
        if (searchText == null || searchText.trim().isEmpty()) {
            supplierList.clear();
            supplierList.addAll(allSuppliers);
            return;
        }

        // Local filtering - no database call!
        String lowerCaseFilter = searchText.toLowerCase();

        List<Company> filtered = allSuppliers.stream()
                .filter(supplier ->
                        supplier.getAddress().getName().toLowerCase().contains(lowerCaseFilter) ||
                                supplier.getAddress().getStreet().toLowerCase().contains(lowerCaseFilter) ||
                                supplier.getAddress().getCity().toLowerCase().contains(lowerCaseFilter) ||
                                supplier.getAddress().getZip().toLowerCase().contains(lowerCaseFilter) ||
                                supplier.getMail().toLowerCase().contains(lowerCaseFilter)
                )
                .toList();

        supplierList.clear();
        supplierList.addAll(filtered);
    }

    /**
     * Opens dialog to add a new supplier (ASYNC save)
     */
    private void addSupplier() {
        SupplierDialog.showAddDialog().ifPresent(newSupplier -> {
            setButtonsEnabled(false);

            // Reload table
            AsyncDatabaseTask.runVoid(
                    () -> {
                        // Database operation in background thread
                        supplierService.save(newSupplier);
                    },
                    this,
                    this::loadSuppliers,
                    error -> {
                        // Error callback
                        DialogUtils.showError("Fehler beim Hinzufügen",
                                "Lieferant konnte nicht gespeichert werden.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        });
    }

    /**
     * Opens dialog to edit selected supplier (ASYNC save)
     */
    private void editSupplier() {
        Company selectedSupplier = tableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) {
            return;
        }

        SupplierDialog.showEditDialog(selectedSupplier).ifPresent(updatedSupplier -> {
            setButtonsEnabled(false);

            AsyncDatabaseTask.runVoid(
                    () -> {
                        // Database operation in background thread
                        supplierService.save(updatedSupplier);
                    },
                    this,
                    this::loadSuppliers,
                    error -> {
                        // Error callback
                        DialogUtils.showError("Fehler beim Speichern",
                                "Änderungen konnten nicht gespeichert werden.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        });
    }

    /**
     * Deletes selected supplier after confirmation (ASYNC delete)
     */
    private void deleteSupplier() {
        Company selectedSupplier = tableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) {
            return;
        }

        // Confirmation dialog
        String itemDescription = "Name: " + selectedSupplier.getAddress().getName() + "\n" +
                "Ort: " + selectedSupplier.getAddress().getCity();

        if (DialogUtils.showDeleteConfirmation(itemDescription)) {
            setButtonsEnabled(false);

            AsyncDatabaseTask.run(
                    () -> {
                        // Database operation in background thread
                        return supplierService.deleteById(selectedSupplier.getCompanyId());
                    },
                    this,
                    success -> {
                        // Success callback
                        if (success) {
                            loadSuppliers(); // Reload table
                        } else {
                            DialogUtils.showError("Fehler",
                                    "Lieferant konnte nicht gelöscht werden.\n" +
                                            "Möglicherweise existieren noch Zuweisungen für diesen Lieferanten.");
                            setButtonsEnabled(true);
                        }
                    },
                    error -> {
                        // Error callback
                        DialogUtils.showError("Fehler beim Löschen",
                                "Ein Fehler ist aufgetreten.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        }
    }

    /**
     * Helper method to enable/disable all buttons
     */
    private void setButtonsEnabled(boolean enabled) {
        addButton.setDisable(!enabled);
        refreshButton.setDisable(!enabled);

        // Edit and delete depend on selection
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