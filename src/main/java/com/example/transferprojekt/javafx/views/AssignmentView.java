package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.javafx.dialogs.AssignmentDialog;
import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.javafx.utils.DialogUtils;
import com.example.transferprojekt.services.AssignmentService;
import com.example.transferprojekt.services.SupplierService;
import com.example.transferprojekt.services.SupplierNrService;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * View for managing supplier assignments.
 */
public class AssignmentView extends BaseView<Assignment> {

    private final AssignmentService assignmentService;
    private final SupplierService supplierService;
    private final SupplierNrService supplierNrService;

    public AssignmentView(AssignmentService assignmentService,
                          SupplierService supplierService,
                          SupplierNrService supplierNrService) {
        this.assignmentService = assignmentService;
        this.supplierService = supplierService;
        this.supplierNrService = supplierNrService;
    }

    @Override
    protected void setupTableView(TableView<Assignment> table) {
        TableColumn<Assignment, String> supplierNameColumn = new TableColumn<>("Lieferant");
        supplierNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSupplierName())
        );
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
    }

    @Override
    protected Callable<List<Assignment>> getLoadTask() {
        return assignmentService::getDatabaseEntries;
    }

    @Override
    protected boolean filterMatch(Assignment assignment, String lowerCaseFilter) {
        boolean matchesSupplierNumber = String.valueOf(assignment.getSupplierNumber().getId())
                .contains(lowerCaseFilter);

        boolean matchesSupplierName = false;
        String supplierName = assignment.getSupplierName();
        if (supplierName != null) {
            matchesSupplierName = supplierName.toLowerCase().contains(lowerCaseFilter);
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
    }

    @Override
    protected void onAdd() {
        AssignmentDialog.showAddDialog(supplierService, supplierNrService, assignmentService).ifPresent(newAssignment -> {
            setButtonsEnabled(false);
            AsyncDatabaseTask.runVoid(
                    () -> assignmentService.save(newAssignment),
                    this,
                    this::loadData,
                    error -> {
                        DialogUtils.showError("Fehler beim Hinzufügen",
                                "Zuweisung konnte nicht gespeichert werden.\n" + error.getMessage());
                        setButtonsEnabled(true);
                    }
            );
        });
    }

    @Override
    protected void onEdit() {
        Assignment selectedAssignment = tableView.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) return;

        AssignmentDialog.showEditDialog(selectedAssignment, supplierService, supplierNrService, assignmentService)
                .ifPresent(updatedAssignment -> {
                    setButtonsEnabled(false);
                    AsyncDatabaseTask.runVoid(
                            () -> assignmentService.save(updatedAssignment),
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
        Assignment selectedAssignment = tableView.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) return;

        String supplierName = selectedAssignment.getSupplierName();
        if (supplierName == null) supplierName = "Unbekannt";

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
                            loadData();
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

    @Override
    protected String getLoadErrorMessage() {
        return "Zuweisungen konnten nicht geladen werden.";
    }
}