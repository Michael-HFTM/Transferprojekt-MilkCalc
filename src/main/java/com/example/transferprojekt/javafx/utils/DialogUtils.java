package com.example.transferprojekt.javafx.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility class for showing standardized dialogs
 */
public class DialogUtils {

    /**
     * Shows an error dialog
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows an error dialog with default title
     */
    public static void showError(String header, String content) {
        showError("Fehler", header, content);
    }

    /**
     * Shows an info dialog
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows an info dialog with default title
     */
    public static void showInfo(String header, String content) {
        showInfo("Information", header, content);
    }

    /**
     * Shows a warning dialog
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a delete confirmation dialog with standard text
     */
    public static boolean showDeleteConfirmation(String itemDescription) {
        return showConfirmation(
                "Löschen bestätigen",
                "Wirklich löschen?",
                itemDescription + "\n\nDiese Aktion kann nicht rückgängig gemacht werden!"
        );
    }
}