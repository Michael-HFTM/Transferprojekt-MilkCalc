package com.example.transferprojekt.javafx.dialogs;

import com.example.transferprojekt.dataclasses.MilkDelivery;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.enumerations.TimeWindow;
import com.example.transferprojekt.services.SupplierNrService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MilkDeliveryDialog extends Dialog<MilkDelivery> {

    // Form fields
    private ComboBox<SupplierNumber> supplierNumberComboBox;
    private DatePicker datePicker;
    private ComboBox<TimeWindow> timeWindowComboBox;
    private TextField amountField;

    // Service
    private final SupplierNrService supplierNrService;

    // Existing delivery (for edit mode)
    private final MilkDelivery existingDelivery;
    private final boolean isEditMode;

    /**
     * Constructor for ADD mode
     */
    private MilkDeliveryDialog(SupplierNrService supplierNrService) {
        this(null, supplierNrService);
    }

    /**
     * Constructor for EDIT mode
     */
    private MilkDeliveryDialog(MilkDelivery existingDelivery, SupplierNrService supplierNrService) {
        this.existingDelivery = existingDelivery;
        this.supplierNrService = supplierNrService;
        this.isEditMode = existingDelivery != null;

        setupDialog();
        createForm();
        setupValidation();
        setupResultConverter();

        if (isEditMode) {
            populateFields();
        }
    }

    private void setupDialog() {
        setTitle(isEditMode ? "Milchlieferung bearbeiten" : "Neue Milchlieferung");
        setHeaderText(isEditMode ?
                "Bearbeiten Sie die Lieferungs-Daten:" :
                "Geben Sie die Daten der neuen Lieferung ein:");

        // Buttons
        ButtonType saveButtonType = new ButtonType(
                isEditMode ? "Speichern" : "Hinzufügen",
                ButtonBar.ButtonData.OK_DONE
        );
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Disable save button initially for add mode
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

        // Load supplier numbers
        try {
            List<SupplierNumber> supplierNumbers = supplierNrService.getDatabaseEntries();
            supplierNumberComboBox.getItems().addAll(supplierNumbers);

            // Custom display
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

        } catch (Exception e) {
            showError("Fehler beim Laden der Lieferantennummern: " + e.getMessage());
        }

        grid.add(new Label("Lieferantennummer:"), 0, 0);
        grid.add(supplierNumberComboBox, 1, 0);

        // Date Picker
        datePicker = new DatePicker();
        datePicker.setPromptText("Lieferdatum");
        datePicker.setValue(LocalDate.now()); // Default: today
        grid.add(new Label("Datum:"), 0, 1);
        grid.add(datePicker, 1, 1);

        // Time Window ComboBox
        timeWindowComboBox = new ComboBox<>();
        timeWindowComboBox.setPromptText("Zeitfenster auswählen");
        timeWindowComboBox.getItems().addAll(TimeWindow.values());

        // Custom display for time window
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
        grid.add(new Label("Menge (kg):"), 0, 3);
        grid.add(amountField, 1, 3);

        getDialogPane().setContent(grid);

        // Request focus on supplier number combo
        supplierNumberComboBox.requestFocus();
    }

    private void setupValidation() {
        Button saveButton = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().get(0)
        );

        // Add listeners for validation
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

        // Visual feedback for date
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                datePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                datePicker.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // Visual feedback for amount
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                amountField.setStyle("");
            } else if (isValidAmount(newVal)) {
                amountField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            } else {
                amountField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
        });

        // Add date picker validation
        addDatePickerValidation(datePicker);
    }

    private boolean isFormValid() {
        // Supplier number must be selected
        if (supplierNumberComboBox.getValue() == null) {
            return false;
        }

        // Date must be set
        if (datePicker.getValue() == null) {
            return false;
        }

        // Time window must be selected
        if (timeWindowComboBox.getValue() == null) {
            return false;
        }

        // Amount must be valid
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
            return amount.compareTo(BigDecimal.ZERO) > 0; // Must be positive
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void populateFields() {
        if (existingDelivery != null) {
            // Find and select supplier number
            int supplierNrId = existingDelivery.getSupplierNumber().getId();
            supplierNumberComboBox.getItems().stream()
                    .filter(sn -> sn.getId() == supplierNrId)
                    .findFirst()
                    .ifPresent(supplierNumberComboBox::setValue);

            // Set date
            datePicker.setValue(existingDelivery.getDate());

            // Set time window
            timeWindowComboBox.setValue(existingDelivery.getTimeWindow());

            // Set amount
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
                    // Edit mode: preserve existing UUID
                    return new MilkDelivery(
                            existingDelivery.getDeliveryId(),
                            amount,
                            date,
                            selectedSupplierNumber,
                            timeWindow
                    );
                } else {
                    // Add mode: no UUID (will be generated by DB)
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

                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Ungültiges Datum");
                    alert.setHeaderText("Datum konnte nicht gelesen werden");
                    alert.setContentText("Bitte verwenden Sie das Format: TT.MM.JJJJ\nBeispiel: 01.12.2025");
                    alert.showAndWait();
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText("Ein Fehler ist aufgetreten");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Static method to show Add dialog
     */
    public static Optional<MilkDelivery> showAddDialog(SupplierNrService supplierNrService) {
        MilkDeliveryDialog dialog = new MilkDeliveryDialog(supplierNrService);
        return dialog.showAndWait();
    }

    /**
     * Static method to show Edit dialog
     */
    public static Optional<MilkDelivery> showEditDialog(MilkDelivery delivery,
                                                        SupplierNrService supplierNrService) {
        MilkDeliveryDialog dialog = new MilkDeliveryDialog(delivery, supplierNrService);
        return dialog.showAndWait();
    }
}