package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.Company;
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

    public SupplierView(SupplierService supplierService) {
        this.supplierService = supplierService;
        this.supplierList = FXCollections.observableArrayList();

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

        // Double-click to edit
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                editSupplier();
            }
        });

        return table;
    }

    /**
     * Loads all suppliers from database
     */
    private void loadSuppliers() {
        try {
            List<Company> suppliers = supplierService.getDatabaseEntries();
            supplierList.clear();
            supplierList.addAll(suppliers);

            // Clear search field when reloading
            searchField.clear();

        } catch (Exception e) {
            showErrorDialog("Fehler beim Laden", "Lieferanten konnten nicht geladen werden.", e.getMessage());
        }
    }

    /**
     * Filters suppliers based on search text
     */
    private void filterSuppliers(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadSuppliers();
            return;
        }

        try {
            List<Company> allSuppliers = supplierService.getDatabaseEntries();
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

        } catch (Exception e) {
            showErrorDialog("Fehler bei der Suche", "Suche konnte nicht durchgeführt werden.", e.getMessage());
        }
    }

    /**
     * Opens dialog to add a new supplier
     */
    private void addSupplier() {
        // TODO: Will be implemented in next step
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hinzufügen");
        alert.setHeaderText("Neuen Lieferanten hinzufügen");
        alert.setContentText("Diese Funktion wird im nächsten Schritt implementiert.");
        alert.showAndWait();
    }

    /**
     * Opens dialog to edit selected supplier
     */
    private void editSupplier() {
        Company selectedSupplier = tableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) {
            return;
        }

        // TODO: Will be implemented in next step
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bearbeiten");
        alert.setHeaderText("Lieferanten bearbeiten");
        alert.setContentText("Ausgewählt: " + selectedSupplier.getAddress().getName() + "\n\nDiese Funktion wird im nächsten Schritt implementiert.");
        alert.showAndWait();
    }

    /**
     * Deletes selected supplier after confirmation
     */
    private void deleteSupplier() {
        Company selectedSupplier = tableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) {
            return;
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Löschen bestätigen");
        confirmation.setHeaderText("Lieferanten wirklich löschen?");
        confirmation.setContentText(
                "Name: " + selectedSupplier.getAddress().getName() + "\n" +
                        "Ort: " + selectedSupplier.getAddress().getCity() + "\n\n" +
                        "Diese Aktion kann nicht rückgängig gemacht werden!"
        );

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = supplierService.deleteById(selectedSupplier.getCompanyId());

                    if (success) {
                        showInfoDialog("Erfolg", "Lieferant wurde gelöscht.");
                        loadSuppliers(); // Reload table
                    } else {
                        showErrorDialog("Fehler", "Lieferant konnte nicht gelöscht werden.",
                                "Möglicherweise existieren noch Zuweisungen für diesen Lieferanten.");
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