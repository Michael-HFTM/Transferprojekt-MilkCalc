package com.example.transferprojekt.javafx.dialogs;

import com.example.transferprojekt.dataclasses.MilkDelivery;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.enumerations.TimeWindow;
import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.javafx.utils.DialogUtils;
import com.example.transferprojekt.services.SupplierNrService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class MilkDeliveryDialog extends Dialog<MilkDelivery> {

    private ComboBox<SupplierNumber> supplierNumberComboBox;
    private DatePicker datePicker;
    private ComboBox<TimeWindow> timeWindowComboBox;
    private TextField amountField;

    private final SupplierNrService supplierNrService;

    private final MilkDelivery existingDelivery;
    private final boolean isEditMode;

    private MilkDeliveryDialog(SupplierNrService supplierNrService) {
        this(null, supplierNrService);
    }

    private MilkDeliveryDialog(MilkDelivery existingDelivery, SupplierNrService supplierNrService) {
        this.existingDelivery = existingDelivery;
        this.supplierNrService = supplierNrService;
        this.isEditMode = existingDelivery != null;

        setupDialog();
        createForm();
        setupValidation();
        setupResultConverter();

        loadSupplierNumbersAsync();

        if (isEditMode) {
            populateFieldsWhenDataLoaded();
        }
    }

    private void setupDialog() {
        setTitle(isEditMode ? "Milchlieferung bearbeiten" : "Neue Milchlieferung");
        setHeaderText(isEditMode ?
                "Bearbeiten Sie die Lieferungs-Daten:" :
                "Geben Sie die Daten der neuen Lieferung ein:");

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

        // Supplier Number ComboBox
        supplierNumberComboBox = new ComboBox<>();
        supplierNumberComboBox.setPromptText("Lieferantennummer auswählen");
        supplierNumberComboBox.setPrefWidth(200);
        supplierNumberComboBox.setDisable(true); // Disabled until data loaded

        grid.add(new Label("Lieferantennummer:"), 0, 0);
        grid.add(supplierNumberComboBox, 1, 0);

        // Date Picker
        datePicker = new DatePicker();
        datePicker.setPromptText("Lieferdatum");
        datePicker.setValue(LocalDate.now());
        datePicker.setPrefWidth(200);
        datePicker.setStyle("-fx-border-color: transparent; -fx-border-radius: 2px;");
        grid.add(new Label("Datum:"), 0, 1);
        grid.add(datePicker, 1, 1);

        // Time Window ComboBox
        timeWindowComboBox = new ComboBox<>();
        timeWindowComboBox.setPromptText("Zeitfenster auswählen");
        timeWindowComboBox.getItems().addAll(TimeWindow.values());
        timeWindowComboBox.setPrefWidth(200);

        timeWindowComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(TimeWindow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item == TimeWindow.MORGEN ? "Morgen" : "Abend");
                }
            }
        });

        timeWindowComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TimeWindow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item == TimeWindow.MORGEN ? "Morgen" : "Abend");
                }
            }
        });

        grid.add(new Label("Zeitfenster:"), 0, 2);
        grid.add(timeWindowComboBox, 1, 2);

        // Amount TextField
        amountField = new TextField();
        amountField.setPromptText("z.B. 150.50");
        amountField.setPrefWidth(200);
        amountField.setStyle("-fx-border-color: transparent; -fx-border-radius: 2px;");
        grid.add(new Label("Menge (kg):"), 0, 3);
        grid.add(amountField, 1, 3);

        getDialogPane().setContent(grid);
        supplierNumberComboBox.requestFocus();
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

        supplierNumberComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        datePicker.valueProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        timeWindowComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        amountField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                datePicker.setStyle("-fx-border-color: red; -fx-border-radius: 2px;");
            } else {
                datePicker.setStyle("-fx-border-color: green; -fx-border-radius: 2px;");
            }
        });

        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                amountField.setStyle("-fx-border-color: transparent; -fx-border-radius: 2px;");
            } else if (isValidAmount(newVal)) {
                amountField.setStyle("-fx-border-color: green; -fx-border-radius: 2px;");
            } else {
                amountField.setStyle("-fx-border-color: red; -fx-border-radius: 2px;");
            }
        });

        addDatePickerValidation(datePicker);
    }

    private boolean isFormValid() {
        if (supplierNumberComboBox.getValue() == null) {
            return false;
        }

        if (datePicker.getValue() == null) {
            return false;
        }

        if (timeWindowComboBox.getValue() == null) {
            return false;
        }

        if (!isValidAmount(amountField.getText())) {
            return false;
        }

        return true;
    }

    private boolean isValidAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        try {
            BigDecimal amount = new BigDecimal(text.trim());
            return amount.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void populateFieldsWhenDataLoaded() {
        if (existingDelivery != null) {
            new Thread(() -> {
                for (int i = 0; i < 50; i++) {
                    if (!supplierNumberComboBox.getItems().isEmpty()) {
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
        if (existingDelivery != null) {
            int supplierNrId = existingDelivery.getSupplierNumber().getId();
            supplierNumberComboBox.getItems().stream()
                    .filter(sn -> sn.getId() == supplierNrId)
                    .findFirst()
                    .ifPresent(supplierNumberComboBox::setValue);

            datePicker.setValue(existingDelivery.getDate());
            timeWindowComboBox.setValue(existingDelivery.getTimeWindow());
            amountField.setText(existingDelivery.getAmountKg().toString());
        }
    }

    private void setupResultConverter() {
        setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                SupplierNumber selectedSupplierNumber = supplierNumberComboBox.getValue();
                LocalDate date = datePicker.getValue();
                TimeWindow timeWindow = timeWindowComboBox.getValue();
                BigDecimal amount = new BigDecimal(amountField.getText().trim());

                if (isEditMode) {
                    return new MilkDelivery(
                            existingDelivery.getDeliveryId(),
                            amount,
                            date,
                            selectedSupplierNumber,
                            timeWindow
                    );
                } else {
                    return new MilkDelivery(
                            amount,
                            date,
                            selectedSupplierNumber,
                            timeWindow
                    );
                }
            }
            return null;
        });
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

    public static Optional<MilkDelivery> showAddDialog(SupplierNrService supplierNrService) {
        MilkDeliveryDialog dialog = new MilkDeliveryDialog(supplierNrService);
        return dialog.showAndWait();
    }

    public static Optional<MilkDelivery> showEditDialog(MilkDelivery delivery,
                                                        SupplierNrService supplierNrService) {
        MilkDeliveryDialog dialog = new MilkDeliveryDialog(delivery, supplierNrService);
        return dialog.showAndWait();
    }
}