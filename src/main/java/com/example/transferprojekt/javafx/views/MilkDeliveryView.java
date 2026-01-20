package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.MilkDelivery;
import com.example.transferprojekt.enumerations.TimeWindow;
import com.example.transferprojekt.javafx.dialogs.MilkDeliveryDialog;
import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.javafx.utils.DialogUtils;
import com.example.transferprojekt.services.MilkDeliveryService;
import com.example.transferprojekt.services.SupplierNrService;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * View for managing milk deliveries.
 */
public class MilkDeliveryView extends BaseView<MilkDelivery> {
    private final MilkDeliveryService milkDeliveryService;
    private final SupplierNrService supplierNrService;
    private final com.example.transferprojekt.services.AssignmentService assignmentService;

    public MilkDeliveryView(MilkDeliveryService milkDeliveryService,
                            SupplierNrService supplierNrService,
                            com.example.transferprojekt.services.AssignmentService assignmentService) {
        this.milkDeliveryService = milkDeliveryService;
        this.supplierNrService = supplierNrService;
        this.assignmentService = assignmentService;
    }

    @Override
    protected void setupTableView(TableView<MilkDelivery> table) {
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
    }

    @Override
    protected Callable<List<MilkDelivery>> getLoadTask() {
        return milkDeliveryService::getDatabaseEntries;
    }

    @Override
    protected boolean filterMatch(MilkDelivery delivery, String lowerCaseFilter) {
        boolean matchesSupplierNumber = String.valueOf(delivery.getSupplierNumber().getId())
                .contains(lowerCaseFilter);

        boolean matchesDate = delivery.getDate().toString().contains(lowerCaseFilter);

        boolean matchesTimeWindow = delivery.getTimeWindow().toString()
                .toLowerCase().contains(lowerCaseFilter);

        boolean matchesAmount = delivery.getAmountKg().toString().contains(lowerCaseFilter);

        return matchesSupplierNumber || matchesDate || matchesTimeWindow || matchesAmount;
    }

    @Override
    protected void onAdd() {
        MilkDeliveryDialog.showAddDialog(supplierNrService, assignmentService).ifPresent(newDelivery -> {
            setButtonsEnabled(false);
            AsyncDatabaseTask.runVoid(
                    () -> milkDeliveryService.save(newDelivery),
                    this,
                    this::loadData,
                    error -> {
                        DialogUtils.showError("Fehler beim Hinzufügen",
                                "Milchlieferung konnte nicht gespeichert werden.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        });
    }

    @Override
    protected void onEdit() {
        MilkDelivery selectedDelivery = tableView.getSelectionModel().getSelectedItem();
        if (selectedDelivery == null) return;

        MilkDeliveryDialog.showEditDialog(selectedDelivery, supplierNrService, assignmentService)
                .ifPresent(updatedDelivery -> {
                    setButtonsEnabled(false);
                    AsyncDatabaseTask.runVoid(
                            () -> milkDeliveryService.save(updatedDelivery),
                            this,
                            this::loadData,
                            error -> {
                                DialogUtils.showError("Fehler beim Speichern",
                                        "Änderungen konnten nicht gespeichert werden.\n" + error.getMessage());
                                setButtonsEnabled(true);
                            }
                    );
                });
    }

    @Override
    protected void onDelete() {
        MilkDelivery selectedDelivery = tableView.getSelectionModel().getSelectedItem();
        if (selectedDelivery == null) return;

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
                            loadData();
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

    @Override
    protected String getLoadErrorMessage() {
        return "Milchlieferungen konnten nicht geladen werden.";
    }
}