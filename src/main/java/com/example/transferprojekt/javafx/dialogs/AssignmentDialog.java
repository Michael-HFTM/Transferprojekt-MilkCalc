package com.example.transferprojekt.javafx.dialogs;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.services.SupplierService;
import com.example.transferprojekt.services.SupplierNrService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AssignmentDialog extends Dialog<Assignment> {

    // Form fields
    private ComboBox<Company> supplierComboBox;
    private ComboBox<SupplierNumber> supplierNumberComboBox;
    private DatePicker validFromDatePicker;
    private DatePicker validToDatePicker;

    // Services
    private final SupplierService supplierService;
    private final SupplierNrService supplierNrService;

    // Existing assignment (for edit mode)
    private final Assignment existingAssignment;
    private final boolean isEditMode;

    /**
     * Constructor for ADD mode
     */
    private AssignmentDialog(SupplierService supplierService, SupplierNrService supplierNrService) {
        this(null, supplierService, supplierNrService);
    }

    /**
     * Constructor for EDIT mode
     */
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

        if (isEditMode) {
            populateFields();
        }
    }

    private void setupDialog() {
        setTitle(isEditMode ? "Zuweisung bearbeiten" : "Neue Zuweisung");
        setHeaderText(isEditMode ?
                "Bearbeiten Sie die Zuweisungs-Daten:" :
                "Geben Sie die Daten der neuen Zuweisung ein:");

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

        // Supplier ComboBox
        supplierComboBox = new ComboBox<>();
        supplierComboBox.setPromptText("Lieferant auswählen");
        supplierComboBox.setPrefWidth(300);

        // Load suppliers
        try {
            List<Company> suppliers = supplierService.getDatabaseEntries();
            supplierComboBox.getItems().addAll(suppliers);

            // Custom display: Show name instead of toString()
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

        } catch (Exception e) {
            showError("Fehler beim Laden der Lieferanten: " + e.getMessage());
        }

        grid.add(new Label("Lieferant:"), 0, 0);
        grid.add(supplierComboBox, 1, 0);

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

        grid.add(new Label("Lieferantennummer:"), 0, 1);
        grid.add(supplierNumberComboBox, 1, 1);

        // Valid From DatePicker
        validFromDatePicker = new DatePicker();
        validFromDatePicker.setPromptText("Gültig ab");
        validFromDatePicker.setValue(LocalDate.now()); // Default: today
        grid.add(new Label("Gültig ab:"), 0, 2);
        grid.add(validFromDatePicker, 1, 2);

        // Valid To DatePicker
        validToDatePicker = new DatePicker();
        validToDatePicker.setPromptText("Gültig bis (optional - leer = unbefristet)");
        grid.add(new Label("Gültig bis:"), 0, 3);
        grid.add(validToDatePicker, 1, 3);

        getDialogPane().setContent(grid);

        // Request focus on supplier combo
        supplierComboBox.requestFocus();
    }

    private void setupValidation() {
        Button saveButton = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().get(0)
        );

        // Add listeners for validation
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

        // Visual feedback for date validation
        validFromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                validFromDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                validFromDatePicker.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        validToDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                // Optional field - no border
                validToDatePicker.setStyle("");
            } else {
                // Check if after validFrom
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

        // Also update validTo border when validFrom changes
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

        // Add text field listener to catch invalid manual input
        addDatePickerValidation(validFromDatePicker);
        addDatePickerValidation(validToDatePicker);
    }

    /**
     * Adds validation to DatePicker to handle invalid manual input
     */
    private void addDatePickerValidation(DatePicker datePicker) {
        datePicker.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                // When focus is lost, validate input
                try {
                    String text = datePicker.getEditor().getText();
                    if (text != null && !text.trim().isEmpty()) {
                        // Try to parse the date
                        datePicker.setValue(datePicker.getConverter().fromString(text));
                    }
                } catch (Exception e) {
                    // Invalid input - reset to previous value or null
                    datePicker.setValue(null);
                    datePicker.getEditor().clear();

                    // Show error
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Ungültiges Datum");
                    alert.setHeaderText("Datum konnte nicht gelesen werden");
                    alert.setContentText("Bitte verwenden Sie das Format: TT.MM.JJJJ\nBeispiel: 01.12.2025");
                    alert.showAndWait();
                }
            }
        });
    }

    private boolean isFormValid() {
        // Supplier must be selected
        if (supplierComboBox.getValue() == null) {
            return false;
        }

        // Supplier number must be selected
        if (supplierNumberComboBox.getValue() == null) {
            return false;
        }

        // Valid from must be set
        if (validFromDatePicker.getValue() == null) {
            return false;
        }

        // Valid to is OPTIONAL - if set, must be after valid from
        if (validToDatePicker.getValue() != null) {
            if (!validToDatePicker.getValue().isAfter(validFromDatePicker.getValue())) {
                return false;
            }
        }

        return true;
    }

    private void populateFields() {
        if (existingAssignment != null) {
            // Find and select supplier
            UUID supplierId = existingAssignment.getSupplierId();
            supplierComboBox.getItems().stream()
                    .filter(c -> c.getCompanyId().equals(supplierId))
                    .findFirst()
                    .ifPresent(supplierComboBox::setValue);

            // Find and select supplier number
            int supplierNrId = existingAssignment.getSupplierNumber().getId();
            supplierNumberComboBox.getItems().stream()
                    .filter(sn -> sn.getId() == supplierNrId)
                    .findFirst()
                    .ifPresent(supplierNumberComboBox::setValue);

            // Set dates
            validFromDatePicker.setValue(existingAssignment.getValidFrom());

            // validTo is optional - can be null
            if (existingAssignment.getValidTo() != null) {
                validToDatePicker.setValue(existingAssignment.getValidTo());
            }
            // If null, DatePicker stays empty
        }
    }

    private void setupResultConverter() {
        setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Company selectedSupplier = supplierComboBox.getValue();
                SupplierNumber selectedSupplierNumber = supplierNumberComboBox.getValue();
                LocalDate validFrom = validFromDatePicker.getValue();
                LocalDate validTo = validToDatePicker.getValue(); // Can be null!

                if (isEditMode) {
                    // Edit mode: preserve existing UUID
                    return new Assignment(
                            existingAssignment.getAssignmentId(),
                            selectedSupplier.getCompanyId(),
                            selectedSupplierNumber,
                            validFrom,
                            validTo
                    );
                } else {
                    // Add mode: no UUID (will be generated by DB)
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
    public static Optional<Assignment> showAddDialog(SupplierService supplierService,
                                                     SupplierNrService supplierNrService) {
        AssignmentDialog dialog = new AssignmentDialog(supplierService, supplierNrService);
        return dialog.showAndWait();
    }

    /**
     * Static method to show Edit dialog
     */
    public static Optional<Assignment> showEditDialog(Assignment assignment,
                                                      SupplierService supplierService,
                                                      SupplierNrService supplierNrService) {
        AssignmentDialog dialog = new AssignmentDialog(assignment, supplierService, supplierNrService);
        return dialog.showAndWait();
    }
}