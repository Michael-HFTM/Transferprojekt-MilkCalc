package com.example.transferprojekt.javafx.utils;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Demo application to test AsyncDatabaseTask and LoadingOverlay
 *
 * This simulates a slow database operation to demonstrate:
 * 1. GUI stays responsive during operation
 * 2. Loading overlay shows visual feedback
 * 3. Success/error callbacks work correctly
 */
public class AsyncDatabaseTaskDemo extends Application {

    private Label resultLabel;

    @Override
    public void start(Stage primaryStage) {
        // Create root as StackPane to support overlay
        StackPane root = new StackPane();

        // Main content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(50));

        Label titleLabel = new Label("AsyncDatabaseTask Demo");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        resultLabel = new Label("Klicken Sie auf einen Button um zu starten");
        resultLabel.setStyle("-fx-font-size: 14px;");
        resultLabel.setWrapText(true);
        resultLabel.setMaxWidth(400);

        // Button 1: Simulate successful operation
        Button successButton = new Button("Simuliere erfolgreiche DB-Operation (3s)");
        successButton.setPrefWidth(300);
        successButton.setOnAction(e -> testSuccessfulOperation(root));

        // Button 2: Simulate failed operation
        Button failButton = new Button("Simuliere fehlgeschlagene DB-Operation (2s)");
        failButton.setPrefWidth(300);
        failButton.setOnAction(e -> testFailedOperation(root));

        // Button 3: Test without overlay
        Button noOverlayButton = new Button("Test ohne Loading-Overlay");
        noOverlayButton.setPrefWidth(300);
        noOverlayButton.setOnAction(e -> testWithoutOverlay());

        content.getChildren().addAll(
                titleLabel,
                resultLabel,
                successButton,
                failButton,
                noOverlayButton
        );

        root.getChildren().add(content);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("AsyncDatabaseTask Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Test successful database operation with loading overlay
     */
    private void testSuccessfulOperation(StackPane root) {
        resultLabel.setText("Operation läuft... (GUI bleibt responsive!)");

        AsyncDatabaseTask.run(
                () -> {
                    // Simulate slow database query
                    Thread.sleep(3000);
                    return "Erfolgreich geladen: 42 Datensätze";
                },
                root,
                result -> {
                    // Success callback
                    resultLabel.setText("✓ Erfolg: " + result);
                    resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");
                },
                error -> {
                    // Error callback
                    resultLabel.setText("✗ Fehler: " + error.getMessage());
                    resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
                }
        );
    }

    /**
     * Test failed database operation with loading overlay
     */
    private void testFailedOperation(StackPane root) {
        resultLabel.setText("Operation läuft... (wird fehlschlagen)");

        AsyncDatabaseTask.run(
                () -> {
                    // Simulate slow database query that fails
                    Thread.sleep(2000);
                    throw new RuntimeException("Datenbankverbindung unterbrochen");
                },
                root,
                result -> {
                    resultLabel.setText("✓ Erfolg: " + result);
                    resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");
                },
                error -> {
                    resultLabel.setText("✗ Fehler: " + error.getMessage());
                    resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
                }
        );
    }

    /**
     * Test operation without loading overlay
     */
    private void testWithoutOverlay() {
        resultLabel.setText("Operation läuft (ohne Overlay)...");

        AsyncDatabaseTask<String> task = new AsyncDatabaseTask<>(() -> {
            Thread.sleep(2000);
            return "Fertig ohne Overlay!";
        });

        task.execute(
                result -> {
                    resultLabel.setText("✓ " + result);
                    resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");
                },
                error -> {
                    resultLabel.setText("✗ Fehler: " + error.getMessage());
                    resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
                }
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}