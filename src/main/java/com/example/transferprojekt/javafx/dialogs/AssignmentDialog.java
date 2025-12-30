package com.example.transferprojekt.javafx.dialogs;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.javafx.utils.DialogUtils;
import com.example.transferprojekt.services.SupplierService;
import com.example.transferprojekt.services.SupplierNrService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

public class AssignmentDialog extends Dialog<Assignment> {

    private ComboBox<Company> supplierComboBox;
    private ComboBox<SupplierNumber> supplierNumberComboBox;
    private DatePicker validFromDatePicker;
    private DatePicker validToDatePicker;

    private final SupplierService supplierService;
    private final SupplierNrService supplierNrService;

    private final Assignment existingAssignment;
    private final boolean isEditMode;

    private AssignmentDialog(SupplierService supplierService, SupplierNrService supplierNrService) {
        this(null, supplierService, supplierNrService);
    }

    private AssignmentDialog(Assignment existingAssignment,
                             SupplierService supplierService,
                             SupplierNrService supplierNrService) {
        this.existingAssignment = existingAssignment;
        this.supplierService = supplierService;
        this.supplierNrService = supplierNrService;
        this.isEditMode = existingAssignment != null;

        setupDialog();
        createForm();
        setupValidation();
        setupResultConverter();

        // Load data asynchronously
        loadSuppliersAsync();
        loadSupplierNumbersAsync();

        if (isEditMode) {
            populateFieldsWhenDataLoaded();
        }
    }

    private void setupDialog() {
        setTitle(isEditMode ? "Zuweisung bearbeiten" : "Neue Zuweisung");
        setHeaderText(isEditMode ?
                "Bearbeiten Sie die Zuweisungs-Daten:" :
                "Geben Sie die Daten der neuen Zuweisung ein:");

        ButtonType saveButtonType = new ButtonType(
                isEditMode ? "Speichern" : "Hinzufügen",
                ButtonBar.ButtonData.OK_DONE
        );
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        if (!isEditMode) {
            saveButton.setDisable(true);
        }
    }

    private void createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Supplier ComboBox
        supplierComboBox = new ComboBox<>();
        supplierComboBox.setPromptText("Lieferant auswählen");
        supplierComboBox.setPrefWidth(300);
        supplierComboBox.setDisable(true); // Disabled until data loaded

        grid.add(new Label("Lieferant:"), 0, 0);
        grid.add(supplierComboBox, 1, 0);

        // Supplier Number ComboBox
        supplierNumberComboBox = new ComboBox<>();
        supplierNumberComboBox.setPromptText("Lieferantennummer auswählen");
        supplierNumberComboBox.setPrefWidth(200);
        supplierNumberComboBox.setDisable(true); // Disabled until data loaded

        grid.add(new Label("Lieferantennummer:"), 0, 1);
        grid.add(supplierNumberComboBox, 1, 1);

        // Valid From DatePicker
        validFromDatePicker = new DatePicker();
        validFromDatePicker.setPromptText("Gültig ab");
        validFromDatePicker.setValue(LocalDate.now());
        grid.add(new Label("Gültig ab:"), 0, 2);
        grid.add(validFromDatePicker, 1, 2);

        // Valid To DatePicker
        validToDatePicker = new DatePicker();
        validToDatePicker.setPromptText("Gültig bis (optional - leer = unbefristet)");
        grid.add(new Label("Gültig bis:"), 0, 3);
        grid.add(validToDatePicker, 1, 3);

        getDialogPane().setContent(grid);
        supplierComboBox.requestFocus();
    }

    private void loadSuppliersAsync() {
        AsyncDatabaseTask.run(
                supplierService::getDatabaseEntries,
                getDialogPane(),
                suppliers -> {
                    supplierComboBox.getItems().addAll(suppliers);

                    supplierComboBox.setCellFactory(param -> new ListCell<>() {
                        @Override
                        protected void updateItem(Company item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getAddress().getName() + " (" + item.getAddress().getCity() + ")");
                            }
                        }
                    });

                    supplierComboBox.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(Company item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getAddress().getName() + " (" + item.getAddress().getCity() + ")");
                            }
                        }
                    });

                    supplierComboBox.setDisable(false);
                },
                error -> DialogUtils.showError("Fehler beim Laden", "Lieferanten konnten nicht geladen werden.\n" + error.getMessage())
        );
    }

    private void loadSupplierNumbersAsync() {
        AsyncDatabaseTask.run(
                supplierNrService::getDatabaseEntries,
                getDialogPane(),
                supplierNumbers -> {
                    supplierNumberComboBox.getItems().addAll(supplierNumbers);

                    supplierNumberComboBox.setCellFactory(param -> new ListCell<>() {
                        @Override
                        protected void updateItem(SupplierNumber item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText("Nummer " + item.getId());
                            }
                        }
                    });

                    supplierNumberComboBox.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(SupplierNumber item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText("Nummer " + item.getId());
                            }
                        }
                    });

                    supplierNumberComboBox.setDisable(false);
                },
                error -> DialogUtils.showError("Fehler beim Laden", "Lieferantennummern konnten nicht geladen werden.\n" + error.getMessage())
        );
    }

    private void setupValidation() {
        Button saveButton = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().getFirst()
        );

        supplierComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        supplierNumberComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        validFromDatePicker.valueProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        validToDatePicker.valueProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );

        validFromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                validFromDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                validFromDatePicker.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        validToDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                validToDatePicker.setStyle("");
            } else {
                LocalDate validFrom = validFromDatePicker.getValue();
                if (validFrom != null && newVal.isAfter(validFrom)) {
                    validToDatePicker.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
                } else if (validFrom != null && !newVal.isAfter(validFrom)) {
                    validToDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                } else {
                    validToDatePicker.setStyle("");
                }
            }
        });

        validFromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            LocalDate validTo = validToDatePicker.getValue();
            if (validTo != null && newVal != null) {
                if (validTo.isAfter(newVal)) {
                    validToDatePicker.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
                } else {
                    validToDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                }
            }
        });

        addDatePickerValidation(validFromDatePicker);
        addDatePickerValidation(validToDatePicker);
    }

    private void addDatePickerValidation(DatePicker datePicker) {
        datePicker.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                try {
                    String text = datePicker.getEditor().getText();
                    if (text != null && !text.trim().isEmpty()) {
                        datePicker.setValue(datePicker.getConverter().fromString(text));
                    }
                } catch (Exception e) {
                    datePicker.setValue(null);
                    datePicker.getEditor().clear();
                    DialogUtils.showWarning("Ungültiges Datum",
                            "Datum konnte nicht gelesen werden",
                            "Bitte verwenden Sie das Format: TT.MM.JJJJ\nBeispiel: 01.12.2025");
                }
            }
        });
    }

    private boolean isFormValid() {
        if (supplierComboBox.getValue() == null) {
            return false;
        }

        if (supplierNumberComboBox.getValue() == null) {
            return false;
        }

        if (validFromDatePicker.getValue() == null) {
            return false;
        }

        if (validToDatePicker.getValue() != null) {
            return validToDatePicker.getValue().isAfter(validFromDatePicker.getValue());
        }

        return true;
    }

    private void populateFieldsWhenDataLoaded() {
        if (existingAssignment != null) {
            // Wait for data to be loaded, then populate
            new Thread(() -> {
                // Wait max 5 seconds for data
                for (int i = 0; i < 50; i++) {
                    if (!supplierComboBox.getItems().isEmpty() &&
                            !supplierNumberComboBox.getItems().isEmpty()) {
                        Platform.runLater(this::populateFields);
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();
        }
    }

    private void populateFields() {
        if (existingAssignment != null) {
            supplierComboBox.getItems().stream()
                    .filter(c -> c.getCompanyId().equals(existingAssignment.getSupplierId()))
                    .findFirst()
                    .ifPresent(supplierComboBox::setValue);

            int supplierNrId = existingAssignment.getSupplierNumber().getId();
            supplierNumberComboBox.getItems().stream()
                    .filter(sn -> sn.getId() == supplierNrId)
                    .findFirst()
                    .ifPresent(supplierNumberComboBox::setValue);

            validFromDatePicker.setValue(existingAssignment.getValidFrom());

            if (existingAssignment.getValidTo() != null) {
                validToDatePicker.setValue(existingAssignment.getValidTo());
            }
        }
    }

    private void setupResultConverter() {
        setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Company selectedSupplier = supplierComboBox.getValue();
                SupplierNumber selectedSupplierNumber = supplierNumberComboBox.getValue();
                LocalDate validFrom = validFromDatePicker.getValue();
                LocalDate validTo = validToDatePicker.getValue();

                if (isEditMode) {
                    return new Assignment(
                            existingAssignment.getAssignmentId(),
                            selectedSupplier.getCompanyId(),
                            selectedSupplierNumber,
                            validFrom,
                            validTo
                    );
                } else {
                    return new Assignment(
                            selectedSupplier.getCompanyId(),
                            selectedSupplierNumber,
                            validFrom,
                            validTo
                    );
                }
            }
            return null;
        });
    }

    public static Optional<Assignment> showAddDialog(SupplierService supplierService,
                                                     SupplierNrService supplierNrService) {
        AssignmentDialog dialog = new AssignmentDialog(supplierService, supplierNrService);
        return dialog.showAndWait();
    }

    public static Optional<Assignment> showEditDialog(Assignment assignment,
                                                      SupplierService supplierService,
                                                      SupplierNrService supplierNrService) {
        AssignmentDialog dialog = new AssignmentDialog(assignment, supplierService, supplierNrService);
        return dialog.showAndWait();
    }
}