package com.example.transferprojekt.javafx.dialogs;

import com.example.transferprojekt.dataclasses.Address;
import com.example.transferprojekt.dataclasses.Company;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class SupplierDialog extends Dialog<Company> {

    // Form fields
    private TextField nameField;
    private TextField streetField;
    private TextField zipField;
    private TextField cityField;
    private TextField emailField;

    // Validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern ZIP_PATTERN = Pattern.compile("^\\d{4}$");

    // Existing company (for edit mode)
    private final Company existingCompany;
    private final boolean isEditMode;

    /**
     * Constructor for ADD mode (new supplier)
     */
    public SupplierDialog() {
        this(null);
    }

    /**
     * Constructor for EDIT mode (existing supplier)
     */
    public SupplierDialog(Company existingCompany) {
        this.existingCompany = existingCompany;
        this.isEditMode = existingCompany != null;

        setupDialog();
        createForm();
        setupValidation();
        setupResultConverter();

        if (isEditMode) {
            populateFields();
        }
    }

    private void setupDialog() {
        setTitle(isEditMode ? "Lieferant bearbeiten" : "Neuer Lieferant");
        setHeaderText(isEditMode ?
                "Bearbeiten Sie die Lieferanten-Daten:" :
                "Geben Sie die Daten des neuen Lieferanten ein:");

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

        // Name field
        nameField = new TextField();
        nameField.setPromptText("z.B. Hof Müller");
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        // Street field
        streetField = new TextField();
        streetField.setPromptText("z.B. Dorfstrasse 12");
        grid.add(new Label("Strasse:"), 0, 1);
        grid.add(streetField, 1, 1);

        // ZIP field
        zipField = new TextField();
        zipField.setPromptText("z.B. 3000");
        zipField.setPrefWidth(100);
        grid.add(new Label("PLZ:"), 0, 2);
        grid.add(zipField, 1, 2);

        // City field
        cityField = new TextField();
        cityField.setPromptText("z.B. Bern");
        grid.add(new Label("Ort:"), 0, 3);
        grid.add(cityField, 1, 3);

        // Email field
        emailField = new TextField();
        emailField.setPromptText("z.B. info@hof-mueller.ch");
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);

        getDialogPane().setContent(grid);

        // Request focus on name field
        nameField.requestFocus();
    }

    private void setupValidation() {
        Button saveButton = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().get(0)
        );

        // Add listeners to all fields for real-time validation
        nameField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        streetField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        zipField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        cityField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );
        emailField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(!isFormValid())
        );

        // Add visual feedback for validation
        zipField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                zipField.setStyle("");
            } else if (ZIP_PATTERN.matcher(newVal).matches()) {
                zipField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            } else {
                zipField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                emailField.setStyle("");
            } else if (EMAIL_PATTERN.matcher(newVal).matches()) {
                emailField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            } else {
                emailField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
        });
    }

    private boolean isFormValid() {
        // All fields must be non-empty
        if (nameField.getText().trim().isEmpty() ||
                streetField.getText().trim().isEmpty() ||
                zipField.getText().trim().isEmpty() ||
                cityField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty()) {
            return false;
        }

        // ZIP must be 4 digits
        if (!ZIP_PATTERN.matcher(zipField.getText().trim()).matches()) {
            return false;
        }

        // Email must be valid
        if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            return false;
        }

        return true;
    }

    private void populateFields() {
        if (existingCompany != null) {
            nameField.setText(existingCompany.getAddress().getName());
            streetField.setText(existingCompany.getAddress().getStreet());
            zipField.setText(existingCompany.getAddress().getZip());
            cityField.setText(existingCompany.getAddress().getCity());
            emailField.setText(existingCompany.getMail());
        }
    }

    private void setupResultConverter() {
        setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                // Create Address object
                Address address = new Address(
                        nameField.getText().trim(),
                        streetField.getText().trim(),
                        cityField.getText().trim(),
                        zipField.getText().trim()
                );

                // Create Company object
                if (isEditMode) {
                    // Edit mode: preserve existing UUID
                    return new Company(
                            existingCompany.getCompanyId(),
                            emailField.getText().trim(),
                            address
                    );
                } else {
                    // Add mode: no UUID (will be generated by DB)
                    return new Company(
                            emailField.getText().trim(),
                            address
                    );
                }
            }
            return null;
        });
    }

    /**
     * Shows validation error dialog
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validierungsfehler");
        alert.setHeaderText("Ungültige Eingabe");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Static method to show Add dialog
     */
    public static Optional<Company> showAddDialog() {
        SupplierDialog dialog = new SupplierDialog();
        return dialog.showAndWait();
    }

    /**
     * Static method to show Edit dialog
     */
    public static Optional<Company> showEditDialog(Company company) {
        SupplierDialog dialog = new SupplierDialog(company);
        return dialog.showAndWait();
    }
}