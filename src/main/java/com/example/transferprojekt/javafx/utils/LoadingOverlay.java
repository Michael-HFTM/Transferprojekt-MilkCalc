package com.example.transferprojekt.javafx.utils;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A loading overlay that can be shown on top of any Region
 * to indicate ongoing background operations.
 */
public class LoadingOverlay extends StackPane {

    private final ProgressIndicator progressIndicator;
    private final Label messageLabel;
    private Region parent;

    /**
     * Creates a new loading overlay with default message
     */
    public LoadingOverlay() {
        this("Lädt...");
    }

    /**
     * Creates a new loading overlay with custom message
     *
     * @param message The message to display
     */
    public LoadingOverlay(String message) {
        // Semi-transparent background
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);

        // Message label
        messageLabel = new Label(message);
        messageLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;"
        );

        // Container for indicator and label
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setStyle(
                "-fx-background-color: rgba(50, 50, 50, 0.9); " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 30;"
        );
        container.getChildren().addAll(progressIndicator, messageLabel);

        getChildren().add(container);
        setAlignment(Pos.CENTER);

        // Initially invisible
        setVisible(false);
        setManaged(false);
    }

    /**
     * Shows the loading overlay on the specified parent region
     *
     * @param parent The parent region to overlay
     */
    public void show(Region parent) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> show(parent));
            return;
        }

        this.parent = parent;

        // If parent is a Pane/Region, add overlay as child
        if (parent instanceof StackPane) {
            StackPane stackPane = (StackPane) parent;
            if (!stackPane.getChildren().contains(this)) {
                stackPane.getChildren().add(this);
            }
        } else if (parent.getParent() instanceof StackPane) {
            StackPane stackPane = (StackPane) parent.getParent();
            if (!stackPane.getChildren().contains(this)) {
                stackPane.getChildren().add(this);
            }
        }

        // Bind size to parent
        prefWidthProperty().bind(parent.widthProperty());
        prefHeightProperty().bind(parent.heightProperty());

        setVisible(true);
        setManaged(true);
        toFront();
    }

    /**
     * Hides and removes the loading overlay
     */
    public void hide() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::hide);
            return;
        }

        setVisible(false);
        setManaged(false);

        // Unbind size
        prefWidthProperty().unbind();
        prefHeightProperty().unbind();

        // Remove from parent
        if (parent != null) {
            if (parent instanceof StackPane) {
                ((StackPane) parent).getChildren().remove(this);
            } else if (parent.getParent() instanceof StackPane) {
                ((StackPane) parent.getParent()).getChildren().remove(this);
            }
        }
    }

    /**
     * Updates the message shown in the overlay
     *
     * @param message The new message
     */
    public void setMessage(String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setMessage(message));
            return;
        }

        messageLabel.setText(message);
    }

    /**
     * Sets the progress (0.0 to 1.0, or negative for indeterminate)
     *
     * @param progress The progress value
     */
    public void setProgress(double progress) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setProgress(progress));
            return;
        }

        progressIndicator.setProgress(progress);
    }
}