package com.example.transferprojekt.javafx;

import com.example.transferprojekt.javafx.views.MainView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();

        // Erstelle MainView (statt Test-View)
        MainView mainView = new MainView();

        // Scene mit groesserem Fenster fuer die Anwendung
        Scene scene = new Scene(mainView, 1200, 800);

        // Fenster konfigurieren
        stage.setScene(scene);
        stage.setTitle("MilkCalc - Milchlieferungs-Verwaltung");
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // Fenster anzeigen
        stage.show();
    }
}