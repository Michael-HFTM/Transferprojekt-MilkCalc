package com.example.transferprojekt.javafx;

import com.example.transferprojekt.javafx.views.MainView;
import com.example.transferprojekt.services.SupplierService;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final SupplierService supplierService;

    public StageInitializer(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();

        // Erstelle MainView mit SupplierService
        MainView mainView = new MainView(supplierService);

        // Scene mit größerem Fenster für die Anwendung
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