package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.MilkDelivery;
import com.example.transferprojekt.enumerations.TimeWindow;
import com.example.transferprojekt.javafx.dialogs.MilkDeliveryDialog;
import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.javafx.utils.DialogUtils;
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
    private List<MilkDelivery> allDeliveries; // Cache for local filtering

    public MilkDeliveryView(MilkDeliveryService milkDeliveryService,
                            SupplierNrService supplierNrService) {
        this.milkDeliveryService = milkDeliveryService;
        this.supplierNrService = supplierNrService;
        this.deliveryList = FXCollections.observableArrayList();
        this.allDeliveries = new java.util.ArrayList<>();

        initializeUI();
        loadDeliveries();
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
        searchField.setPromptText("Suche nach Lieferantennummer, Datum, Zeitfenster...");
        searchField.setPrefWidth(350);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterDeliveries(newVal));

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

        TableColumn<MilkDelivery, UUID> idColumn = new TableColumn<>("Liefer-ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryId"));
        idColumn.setVisible(false);

        TableColumn<MilkDelivery, String> supplierNumberColumn = new TableColumn<>("Lieferantennummer");
        supplierNumberColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(cellData.getValue().getSupplierNumber().getId())
                )
        );
        supplierNumberColumn.setPrefWidth(150);

        TableColumn<MilkDelivery, LocalDate> dateColumn = new TableColumn<>("Datum");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(120);

        TableColumn<MilkDelivery, TimeWindow> timeWindowColumn = new TableColumn<>("Zeitfenster");
        timeWindowColumn.setCellValueFactory(new PropertyValueFactory<>("timeWindow"));
        timeWindowColumn.setPrefWidth(100);

        TableColumn<MilkDelivery, BigDecimal> amountColumn = new TableColumn<>("Menge (kg)");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountKg"));
        amountColumn.setPrefWidth(120);

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

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                editDelivery();
            }
        });

        return table;
    }

    /**
     * Loads all deliveries from database (ASYNC)
     */
    private void loadDeliveries() {
        setButtonsEnabled(false);

        AsyncDatabaseTask.run(
                milkDeliveryService::getDatabaseEntries,
                this,
                deliveries -> {
                    allDeliveries = deliveries;
                    deliveryList.clear();
                    deliveryList.addAll(deliveries);
                    searchField.clear();
                    setButtonsEnabled(true);
                },
                error -> {
                    DialogUtils.showError("Fehler beim Laden", "Milchlieferungen konnten nicht geladen werden.\n" + error.getMessage());
                    setButtonsEnabled(true);
                }
        );
    }

    /**
     * Filters deliveries based on search text (LOCAL)
     */
    private void filterDeliveries(String searchText) {
        if (allDeliveries == null) {
            return;
        }

        if (searchText == null || searchText.trim().isEmpty()) {
            deliveryList.clear();
            deliveryList.addAll(allDeliveries);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();

        List<MilkDelivery> filtered = allDeliveries.stream()
                .filter(delivery -> {
                    boolean matchesSupplierNumber = String.valueOf(delivery.getSupplierNumber().getId())
                            .contains(lowerCaseFilter);

                    boolean matchesDate = delivery.getDate().toString().contains(lowerCaseFilter);

                    boolean matchesTimeWindow = delivery.getTimeWindow().toString()
                            .toLowerCase().contains(lowerCaseFilter);

                    boolean matchesAmount = delivery.getAmountKg().toString().contains(lowerCaseFilter);

                    return matchesSupplierNumber || matchesDate || matchesTimeWindow || matchesAmount;
                })
                .toList();

        deliveryList.clear();
        deliveryList.addAll(filtered);
    }

    /**
     * Opens dialog to add a new delivery (ASYNC)
     */
    private void addDelivery() {
        MilkDeliveryDialog.showAddDialog(supplierNrService).ifPresent(newDelivery -> {
            setButtonsEnabled(false);

            AsyncDatabaseTask.runVoid(
                    () -> milkDeliveryService.save(newDelivery),
                    this,
                    () -> {
                        DialogUtils.showInfo("Erfolg", "Milchlieferung wurde erfolgreich hinzugefügt.");
                        loadDeliveries();
                    },
                    error -> {
                        DialogUtils.showError("Fehler beim Hinzufügen",
                                "Milchlieferung konnte nicht gespeichert werden.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        });
    }

    /**
     * Opens dialog to edit selected delivery (ASYNC)
     */
    private void editDelivery() {
        MilkDelivery selectedDelivery = tableView.getSelectionModel().getSelectedItem();
        if (selectedDelivery == null) {
            return;
        }

        MilkDeliveryDialog.showEditDialog(selectedDelivery, supplierNrService)
                .ifPresent(updatedDelivery -> {
                    setButtonsEnabled(false);

                    AsyncDatabaseTask.runVoid(
                            () -> milkDeliveryService.save(updatedDelivery),
                            this,
                            () -> {
                                DialogUtils.showInfo("Erfolg", "Milchlieferung wurde erfolgreich aktualisiert.");
                                loadDeliveries();
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
     * Deletes selected delivery after confirmation (ASYNC)
     */
    private void deleteDelivery() {
        MilkDelivery selectedDelivery = tableView.getSelectionModel().getSelectedItem();
        if (selectedDelivery == null) {
            return;
        }

        String itemDescription = "Lieferantennummer: " + selectedDelivery.getSupplierNumber().getId() + "\n" +
                "Datum: " + selectedDelivery.getDate() + "\n" +
                "Menge: " + selectedDelivery.getAmountKg() + " kg";

        if (DialogUtils.showDeleteConfirmation(itemDescription)) {
            setButtonsEnabled(false);

            AsyncDatabaseTask.run(
                    () -> milkDeliveryService.deleteById(selectedDelivery.getDeliveryId()),
                    this,
                    success -> {
                        if (success) {
                            DialogUtils.showInfo("Erfolg", "Milchlieferung wurde gelöscht.");
                            loadDeliveries();
                        } else {
                            DialogUtils.showError("Fehler", "Milchlieferung konnte nicht gelöscht werden.\n" +
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