package com.example.transferprojekt.javafx;

import com.example.transferprojekt.javafx.views.MainView;
import com.example.transferprojekt.services.AssignmentService;
import com.example.transferprojekt.services.MilkDeliveryService;
import com.example.transferprojekt.services.SupplierService;
import com.example.transferprojekt.services.SupplierNrService;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final SupplierService supplierService;
    private final AssignmentService assignmentService;
    private final SupplierNrService supplierNrService;
    private final MilkDeliveryService milkDeliveryService;

    public StageInitializer(SupplierService supplierService,
                            AssignmentService assignmentService,
                            SupplierNrService supplierNrService,
                            MilkDeliveryService milkDeliveryService) {
        this.supplierService = supplierService;
        this.assignmentService = assignmentService;
        this.supplierNrService = supplierNrService;
        this.milkDeliveryService = milkDeliveryService;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();

        // Erstelle MainView mit Services
        MainView mainView = new MainView(supplierService, assignmentService, supplierNrService, milkDeliveryService);

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