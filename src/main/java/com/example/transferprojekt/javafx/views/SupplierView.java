package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.javafx.dialogs.SupplierDialog;
import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.javafx.utils.DialogUtils;
import com.example.transferprojekt.services.SupplierService;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * View for managing suppliers.
 */
public class SupplierView extends BaseView<Company> {

    private final SupplierService supplierService;

    public SupplierView(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Override
    protected void setupTableView(TableView<Company> table) {
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
    }

    @Override
    protected Callable<List<Company>> getLoadTask() {
        return supplierService::getDatabaseEntries;
    }

    @Override
    protected boolean filterMatch(Company supplier, String lowerCaseFilter) {
        return supplier.getAddress().getName().toLowerCase().contains(lowerCaseFilter) ||
                supplier.getAddress().getStreet().toLowerCase().contains(lowerCaseFilter) ||
                supplier.getAddress().getCity().toLowerCase().contains(lowerCaseFilter) ||
                supplier.getAddress().getZip().toLowerCase().contains(lowerCaseFilter) ||
                supplier.getMail().toLowerCase().contains(lowerCaseFilter);
    }

    @Override
    protected void onAdd() {
        SupplierDialog.showAddDialog().ifPresent(newSupplier -> {
            setButtonsEnabled(false);
            AsyncDatabaseTask.runVoid(
                    () -> supplierService.save(newSupplier),
                    this,
                    this::loadData,
                    error -> {
                        DialogUtils.showError("Fehler beim Hinzufügen",
                                "Lieferant konnte nicht gespeichert werden.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        });
    }

    @Override
    protected void onEdit() {
        Company selectedSupplier = tableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) return;

        SupplierDialog.showEditDialog(selectedSupplier).ifPresent(updatedSupplier -> {
            setButtonsEnabled(false);
            AsyncDatabaseTask.runVoid(
                    () -> supplierService.save(updatedSupplier),
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
        Company selectedSupplier = tableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) return;

        String itemDescription = "Name: " + selectedSupplier.getAddress().getName() + "\n" +
                "Ort: " + selectedSupplier.getAddress().getCity();

        if (DialogUtils.showDeleteConfirmation(itemDescription)) {
            setButtonsEnabled(false);
            AsyncDatabaseTask.run(
                    () -> supplierService.deleteById(selectedSupplier.getCompanyId()),
                    this,
                    success -> {
                        if (success) {
                            loadData();
                        } else {
                            DialogUtils.showError("Fehler",
                                    "Lieferant konnte nicht gelöscht werden.\n" +
                                            "Möglicherweise existieren noch Zuweisungen für diesen Lieferanten.");
                            setButtonsEnabled(true);
                        }
                    },
                    error -> {
                        DialogUtils.showError("Fehler beim Löschen",
                                "Ein Fehler ist aufgetreten.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        }
    }

    @Override
    protected String getLoadErrorMessage() {
        return "Lieferanten konnten nicht geladen werden.";
    }
}