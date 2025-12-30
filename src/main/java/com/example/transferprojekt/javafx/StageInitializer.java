package com.example.transferprojekt.javafx;

import com.example.transferprojekt.javafx.views.MainView;
import com.example.transferprojekt.services.*;
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
    private final TestdataService testdataService;
    private final AdminToolsService adminToolsService;

    public StageInitializer(SupplierService supplierService,
                            AssignmentService assignmentService,
                            SupplierNrService supplierNrService,
                            MilkDeliveryService milkDeliveryService,
                            TestdataService testdataService,
                            AdminToolsService adminToolsService) {
        this.supplierService = supplierService;
        this.assignmentService = assignmentService;
        this.supplierNrService = supplierNrService;
        this.milkDeliveryService = milkDeliveryService;
        this.testdataService = testdataService;
        this.adminToolsService = adminToolsService;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();

        // Create MainView with all required services
        MainView mainView = new MainView(
                supplierService,
                assignmentService,
                supplierNrService,
                milkDeliveryService,
                testdataService,
                adminToolsService
        );

        // Create scene with appropriate size
        Scene scene = new Scene(mainView, 900, 600);

        // Configure stage
        stage.setScene(scene);
        stage.setTitle("MilkCalc - Milchlieferungs-Verwaltung");
        stage.setMinWidth(600);
        stage.setMinHeight(400);

        // Show stage
        stage.show();
    }
}