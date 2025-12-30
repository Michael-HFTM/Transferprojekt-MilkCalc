package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.services.AdminToolsService;
import com.example.transferprojekt.services.AssignmentService;
import com.example.transferprojekt.services.MilkDeliveryService;
import com.example.transferprojekt.services.SupplierService;
import com.example.transferprojekt.services.SupplierNrService;
import com.example.transferprojekt.services.TestdataService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

public class MainView extends BorderPane {

    private final TabPane tabPane;
    private final MenuBar menuBar;
    private final SupplierService supplierService;
    private final AssignmentService assignmentService;
    private final SupplierNrService supplierNrService;
    private final MilkDeliveryService milkDeliveryService;
    private final TestdataService testdataService;
    private final AdminToolsService adminToolsService;

    private DashboardView dashboardView;

    public MainView(SupplierService supplierService,
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

        menuBar = createMenuBar();
        tabPane = createTabPane();

        setTop(menuBar);
        setCenter(tabPane);
        setPadding(new Insets(0));

        setupTabSelectionListener();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("Datei");
        MenuItem exitItem = new MenuItem("Beenden");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(exitItem);

        Menu dataMenu = new Menu("Daten");
        MenuItem refreshItem = new MenuItem("Aktualisieren");
        refreshItem.setOnAction(e -> refreshAllViews());
        MenuItem testDataItem = new MenuItem("Testdaten einfügen");
        testDataItem.setOnAction(e -> insertTestData());
        MenuItem clearDataItem = new MenuItem("Alle Daten löschen");
        clearDataItem.setOnAction(e -> clearAllData());
        dataMenu.getItems().addAll(refreshItem, new SeparatorMenuItem(), testDataItem, clearDataItem);

        Menu helpMenu = new Menu("Hilfe");
        MenuItem aboutItem = new MenuItem("Über MilkCalc");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, dataMenu, helpMenu);
        return menuBar;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab supplierTab = new Tab("Lieferanten");
        supplierTab.setContent(new SupplierView(supplierService));

        Tab assignmentTab = new Tab("Zuweisungen");
        assignmentTab.setContent(new AssignmentView(assignmentService, supplierService, supplierNrService));

        Tab deliveryTab = new Tab("Milchlieferungen");
        deliveryTab.setContent(new MilkDeliveryView(milkDeliveryService, supplierNrService));

        Tab dashboardTab = new Tab("Dashboard");
        dashboardView = new DashboardView(milkDeliveryService, supplierService, assignmentService);
        dashboardTab.setContent(dashboardView);

        tabPane.getTabs().addAll(dashboardTab, supplierTab, assignmentTab, deliveryTab);

        return tabPane;
    }

    private void setupTabSelectionListener() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && "Dashboard".equals(newTab.getText())) {
                if (dashboardView != null) {
                    dashboardView.refresh();
                }
            }
        });
    }

    private void refreshAllViews() {
        try {
            Tab supplierTab = tabPane.getTabs().get(1);
            supplierTab.setContent(new SupplierView(supplierService));

            Tab assignmentTab = tabPane.getTabs().get(2);
            assignmentTab.setContent(new AssignmentView(assignmentService, supplierService, supplierNrService));

            Tab deliveryTab = tabPane.getTabs().get(3);
            deliveryTab.setContent(new MilkDeliveryView(milkDeliveryService, supplierNrService));

            Tab dashboardTab = tabPane.getTabs().get(0);
            dashboardView = new DashboardView(milkDeliveryService, supplierService, assignmentService);
            dashboardTab.setContent(dashboardView);

            tabPane.getSelectionModel().select(dashboardTab);

        } catch (Exception e) {
            showError("Fehler beim Aktualisieren", "Die Views konnten nicht aktualisiert werden.", e.getMessage());
        }
    }

    private void insertTestData() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Testdaten einfügen");
        confirmation.setHeaderText("Testdaten einfügen?");
        confirmation.setContentText(
                "Dies wird alle vorhandenen Daten löschen und Testdaten einfügen.\n\n" +
                        "Eingefügte Daten:\n" +
                        "- 3 Lieferanten (Hof Müller, Biofarm Huber, Alpenmilch AG)\n" +
                        "- 3 Zuweisungen\n" +
                        "- 4 Milchlieferungen"
        );

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AsyncDatabaseTask.runVoid(
                        () -> {
                            adminToolsService.flushAllDataTables("DELETE");
                            testdataService.insertTestdata();
                        },
                        this,
                        () -> {
                            refreshAllViews();

                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("Erfolg");
                            success.setHeaderText("Testdaten erfolgreich eingefügt");
                            success.setContentText(
                                    "3 Lieferanten, 3 Zuweisungen und 4 Milchlieferungen wurden erstellt.\n\n" +
                                            "Sie befinden sich jetzt im Dashboard mit den aktualisierten Statistiken."
                            );
                            success.showAndWait();
                        },
                        error -> {
                            showError("Fehler", "Testdaten konnten nicht eingefügt werden", error.getMessage());
                            error.printStackTrace();
                        }
                );
            }
        });
    }

    private void clearAllData() {
        Alert confirmation = new Alert(Alert.AlertType.WARNING);
        confirmation.setTitle("Alle Daten löschen");
        confirmation.setHeaderText("WARNUNG: Alle Daten löschen?");
        confirmation.setContentText(
                "Diese Aktion kann nicht rückgängig gemacht werden!\n\n" +
                        "Folgende Daten werden gelöscht:\n" +
                        "- Alle Lieferanten\n" +
                        "- Alle Zuweisungen\n" +
                        "- Alle Milchlieferungen"
        );

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Bestätigung");
                dialog.setHeaderText("Geben Sie 'DELETE' ein, um zu bestätigen:");
                dialog.setContentText("Bestätigung:");

                dialog.showAndWait().ifPresent(input -> {
                    if ("DELETE".equals(input)) {
                        AsyncDatabaseTask.runVoid(
                                () -> adminToolsService.flushAllDataTables("DELETE"),
                                this,
                                () -> {
                                    refreshAllViews();

                                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                                    success.setTitle("Erfolg");
                                    success.setHeaderText("Daten erfolgreich gelöscht");
                                    success.setContentText("Alle Daten wurden erfolgreich aus der Datenbank entfernt!");
                                    success.showAndWait();
                                },
                                error -> {
                                    showError("Fehler", "Daten konnten nicht gelöscht werden", error.getMessage());
                                    error.printStackTrace();
                                }
                        );
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Abgebrochen");
                        error.setHeaderText("Falsche Eingabe");
                        error.setContentText("Die Aktion wurde abgebrochen.");
                        error.showAndWait();
                    }
                });
            }
        });
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Über MilkCalc");
        alert.setHeaderText("MilkCalc - Milchlieferungs-Verwaltung");
        alert.setContentText(
                "Version: 0.0.1-SNAPSHOT\n" +
                        "Entwickelt als Praxisarbeit\n" +
                        "Technologien: JavaFX + Spring Boot + PostgreSQL\n\n" +
                        "© 2025"
        );
        alert.showAndWait();
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Tab getTab(int index) {
        return tabPane.getTabs().get(index);
    }

    public void setTabContent(int index, javafx.scene.Node content) {
        tabPane.getTabs().get(index).setContent(content);
    }
}