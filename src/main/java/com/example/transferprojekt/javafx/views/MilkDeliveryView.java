package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.MilkDelivery;
import com.example.transferprojekt.enumerations.TimeWindow;
import com.example.transferprojekt.javafx.dialogs.MilkDeliveryDialog;
import com.example.transferprojekt.services.MilkDeliveryService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MilkDeliveryView extends BorderPane {

    private final MilkDeliveryService milkDeliveryService;
    private final SupplierNrService supplierNrService;

    // UI Components
    private TableView<MilkDelivery> tableView;
    private TextField searchField;
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button refreshButton;

    // Data
    private ObservableList<MilkDelivery> deliveryList;

    public MilkDeliveryView(MilkDeliveryService milkDeliveryService,
                            SupplierNrService supplierNrService) {
        this.milkDeliveryService = milkDeliveryService;
        this.supplierNrService = supplierNrService;
        this.deliveryList = FXCollections.observableArrayList();

        initializeUI();
        loadDeliveries();
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
        searchField.setPromptText("Suche nach Lieferantennummer, Datum, Zeitfenster...");
        searchField.setPrefWidth(350);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterDeliveries(newVal));

        // Buttons
        addButton = new Button("Hinzufügen");
        addButton.setOnAction(e -> addDelivery());

        editButton = new Button("Bearbeiten");
        editButton.setOnAction(e -> editDelivery());
        editButton.setDisable(true);

        deleteButton = new Button("Löschen");
        deleteButton.setOnAction(e -> deleteDelivery());
        deleteButton.setDisable(true);

        refreshButton = new Button("Aktualisieren");
        refreshButton.setOnAction(e -> loadDeliveries());

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

    private TableView<MilkDelivery> createTableView() {
        TableView<MilkDelivery> table = new TableView<>();
        table.setItems(deliveryList);

        // Column: Delivery ID (hidden)
        TableColumn<MilkDelivery, UUID> idColumn = new TableColumn<>("Liefer-ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryId"));
        idColumn.setVisible(false);

        // Column: Supplier Number
        TableColumn<MilkDelivery, String> supplierNumberColumn = new TableColumn<>("Lieferantennummer");
        supplierNumberColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(cellData.getValue().getSupplierNumber().getId())
                )
        );
        supplierNumberColumn.setPrefWidth(150);

        // Column: Date
        TableColumn<MilkDelivery, LocalDate> dateColumn = new TableColumn<>("Datum");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(120);

        // Column: Time Window
        TableColumn<MilkDelivery, TimeWindow> timeWindowColumn = new TableColumn<>("Zeitfenster");
        timeWindowColumn.setCellValueFactory(new PropertyValueFactory<>("timeWindow"));
        timeWindowColumn.setPrefWidth(100);

        // Column: Amount (kg)
        TableColumn<MilkDelivery, BigDecimal> amountColumn = new TableColumn<>("Menge (kg)");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountKg"));
        amountColumn.setPrefWidth(120);

        // Custom cell factory for amount (format with 2 decimal places)
        amountColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f kg", item.doubleValue()));
                }
            }
        });

        table.getColumns().addAll(
                supplierNumberColumn,
                dateColumn,
                timeWindowColumn,
                amountColumn
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
                editDelivery();
            }
        });

        return table;
    }

    /**
     * Loads all deliveries from database
     */
    private void loadDeliveries() {
        try {
            List<MilkDelivery> deliveries = milkDeliveryService.getDatabaseEntries();
            deliveryList.clear();
            deliveryList.addAll(deliveries);

            // Clear search field
            searchField.clear();

        } catch (Exception e) {
            showErrorDialog("Fehler beim Laden", "Milchlieferungen konnten nicht geladen werden.", e.getMessage());
        }
    }

    /**
     * Filters deliveries based on search text
     */
    private void filterDeliveries(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadDeliveries();
            return;
        }

        try {
            List<MilkDelivery> allDeliveries = milkDeliveryService.getDatabaseEntries();
            String lowerCaseFilter = searchText.toLowerCase();

            List<MilkDelivery> filtered = allDeliveries.stream()
                    .filter(delivery -> {
                        // Search in supplier number
                        boolean matchesSupplierNumber = String.valueOf(delivery.getSupplierNumber().getId())
                                .contains(lowerCaseFilter);

                        // Search in date
                        boolean matchesDate = delivery.getDate().toString().contains(lowerCaseFilter);

                        // Search in time window
                        boolean matchesTimeWindow = delivery.getTimeWindow().toString()
                                .toLowerCase().contains(lowerCaseFilter);

                        // Search in amount
                        boolean matchesAmount = delivery.getAmountKg().toString().contains(lowerCaseFilter);

                        return matchesSupplierNumber || matchesDate || matchesTimeWindow || matchesAmount;
                    })
                    .toList();

            deliveryList.clear();
            deliveryList.addAll(filtered);

        } catch (Exception e) {
            showErrorDialog("Fehler bei der Suche", "Suche konnte nicht durchgeführt werden.", e.getMessage());
        }
    }

    /**
     * Opens dialog to add a new delivery
     */
    private void addDelivery() {
        MilkDeliveryDialog.showAddDialog(supplierNrService).ifPresent(newDelivery -> {
            try {
                milkDeliveryService.save(newDelivery);
                showInfoDialog("Erfolg", "Milchlieferung wurde erfolgreich hinzugefügt.");
                loadDeliveries();

            } catch (Exception e) {
                showErrorDialog("Fehler beim Hinzufügen",
                        "Milchlieferung konnte nicht gespeichert werden.",
                        e.getMessage());
            }
        });
    }

    /**
     * Opens dialog to edit selected delivery
     */
    private void editDelivery() {
        MilkDelivery selectedDelivery = tableView.getSelectionModel().getSelectedItem();
        if (selectedDelivery == null) {
            return;
        }

        MilkDeliveryDialog.showEditDialog(selectedDelivery, supplierNrService)
                .ifPresent(updatedDelivery -> {
                    try {
                        milkDeliveryService.save(updatedDelivery);
                        showInfoDialog("Erfolg", "Milchlieferung wurde erfolgreich aktualisiert.");
                        loadDeliveries();

                    } catch (Exception e) {
                        showErrorDialog("Fehler beim Speichern",
                                "Änderungen konnten nicht gespeichert werden.",
                                e.getMessage());
                    }
                });
    }

    /**
     * Deletes selected delivery after confirmation
     */
    private void deleteDelivery() {
        MilkDelivery selectedDelivery = tableView.getSelectionModel().getSelectedItem();
        if (selectedDelivery == null) {
            return;
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Löschen bestätigen");
        confirmation.setHeaderText("Milchlieferung wirklich löschen?");
        confirmation.setContentText(
                "Lieferantennummer: " + selectedDelivery.getSupplierNumber().getId() + "\n" +
                        "Datum: " + selectedDelivery.getDate() + "\n" +
                        "Menge: " + selectedDelivery.getAmountKg() + " kg\n\n" +
                        "Diese Aktion kann nicht rückgängig gemacht werden!"
        );

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = milkDeliveryService.deleteById(selectedDelivery.getDeliveryId());

                    if (success) {
                        showInfoDialog("Erfolg", "Milchlieferung wurde gelöscht.");
                        loadDeliveries();
                    } else {
                        showErrorDialog("Fehler", "Milchlieferung konnte nicht gelöscht werden.",
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