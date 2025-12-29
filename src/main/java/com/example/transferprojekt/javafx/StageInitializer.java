package com.example.transferprojekt.javafx;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();

        // Erstelle eine einfache Test-View
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("MilkCalc - JavaFX Test View");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Spring Boot + JavaFX erfolgreich integriert!");
        infoLabel.setStyle("-fx-font-size: 16px;");

        Label statusLabel = new Label("✓ Backend läuft");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");

        root.getChildren().addAll(titleLabel, infoLabel, statusLabel);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Transferprojekt - MilkCalc");
        stage.show();
    }
}