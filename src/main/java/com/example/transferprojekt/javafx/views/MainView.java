package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.services.AdminToolsService;
import com.example.transferprojekt.services.AssignmentService;
import com.example.transferprojekt.services.MilkDeliveryService;
import com.example.transferprojekt.services.SupplierService;
import com.example.transferprojekt.services.SupplierNrService;
import com.example.transferprojekt.services.TestdataService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {

    private final TabPane tabPane;
    private final MenuBar menuBar;
    private final SupplierService supplierService;
    private final AssignmentService assignmentService;
    private final SupplierNrService supplierNrService;
    private final MilkDeliveryService milkDeliveryService;
    private final TestdataService testdataService;
    private final AdminToolsService adminToolsService;

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

        // MenuBar erstellen
        menuBar = createMenuBar();

        // TabPane für verschiedene Bereiche erstellen
        tabPane = createTabPane();

        // Layout zusammenbauen
        setTop(menuBar);
        setCenter(tabPane);

        // Styling
        setPadding(new Insets(0));
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Datei-Menü
        Menu fileMenu = new Menu("Datei");
        MenuItem exitItem = new MenuItem("Beenden");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(exitItem);

        // Daten-Menü
        Menu dataMenu = new Menu("Daten");
        MenuItem refreshItem = new MenuItem("Aktualisieren");
        refreshItem.setOnAction(e -> refreshAllViews());
        MenuItem testDataItem = new MenuItem("Testdaten einfügen");
        testDataItem.setOnAction(e -> insertTestData());
        MenuItem clearDataItem = new MenuItem("Alle Daten löschen");
        clearDataItem.setOnAction(e -> clearAllData());
        dataMenu.getItems().addAll(refreshItem, new SeparatorMenuItem(), testDataItem, clearDataItem);

        // Hilfe-Menü
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

        // Tab 1: Lieferanten
        Tab supplierTab = new Tab("Lieferanten");
        supplierTab.setContent(new SupplierView(supplierService));

        // Tab 2: Zuweisungen
        Tab assignmentTab = new Tab("Zuweisungen");
        assignmentTab.setContent(new AssignmentView(assignmentService, supplierService, supplierNrService));

        // Tab 3: Milchlieferungen
        Tab deliveryTab = new Tab("Milchlieferungen");
        deliveryTab.setContent(new MilkDeliveryView(milkDeliveryService, supplierNrService));

        // Tab 4: Dashboard
        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setContent(new DashboardView(milkDeliveryService, supplierService, assignmentService));

        tabPane.getTabs().addAll(dashboardTab, supplierTab, assignmentTab, deliveryTab);

        return tabPane;
    }

    /**
     * Aktualisiert alle Views durch Neuerstellung mit frischen Daten
     */
    private void refreshAllViews() {
        try {
            // Recreate all tabs with fresh data
            Tab supplierTab = tabPane.getTabs().get(1);
            supplierTab.setContent(new SupplierView(supplierService));

            Tab assignmentTab = tabPane.getTabs().get(2);
            assignmentTab.setContent(new AssignmentView(assignmentService, supplierService, supplierNrService));

            Tab deliveryTab = tabPane.getTabs().get(3);
            deliveryTab.setContent(new MilkDeliveryView(milkDeliveryService, supplierNrService));

            Tab dashboardTab = tabPane.getTabs().get(0);
            dashboardTab.setContent(new DashboardView(milkDeliveryService, supplierService, assignmentService));

            // Switch to dashboard to show updated statistics
            tabPane.getSelectionModel().select(dashboardTab);

        } catch (Exception e) {
            showError("Fehler beim Aktualisieren", "Die Views konnten nicht aktualisiert werden.", e.getMessage());
        }
    }

    /**
     * Fügt Testdaten ein (löscht vorher alle bestehenden Daten)
     */
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
                try {
                    // 1. Clear existing data
                    adminToolsService.flushAllDataTables("DELETE");

                    // 2. Insert test data
                    testdataService.insertTestdata();

                    // 3. Refresh all views
                    refreshAllViews();

                    // 4. Show success message
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Erfolg");
                    success.setHeaderText("Testdaten erfolgreich eingefügt");
                    success.setContentText(
                            "3 Lieferanten, 3 Zuweisungen und 4 Milchlieferungen wurden erstellt.\n\n" +
                                    "Sie befinden sich jetzt im Dashboard mit den aktualisierten Statistiken."
                    );
                    success.showAndWait();

                } catch (Exception e) {
                    showError("Fehler", "Testdaten konnten nicht eingefügt werden", e.getMessage());
                    e.printStackTrace(); // For debugging
                }
            }
        });
    }

    /**
     * Löscht alle Daten aus der Datenbank (mit doppelter Bestätigung)
     */
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
                // Second confirmation with text input
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Bestätigung");
                dialog.setHeaderText("Geben Sie 'DELETE' ein, um zu bestätigen:");
                dialog.setContentText("Bestätigung:");

                dialog.showAndWait().ifPresent(input -> {
                    if ("DELETE".equals(input)) {
                        try {
                            // Call AdminToolsService
                            adminToolsService.flushAllDataTables("DELETE");

                            // Refresh all views
                            refreshAllViews();

                            // Show success
                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("Erfolg");
                            success.setHeaderText("Daten erfolgreich gelöscht");
                            success.setContentText("Alle Daten wurden erfolgreich aus der Datenbank entfernt!");
                            success.showAndWait();

                        } catch (Exception e) {
                            showError("Fehler", "Daten konnten nicht gelöscht werden", e.getMessage());
                            e.printStackTrace(); // For debugging
                        }
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

    /**
     * Zeigt einen "Über"-Dialog
     */
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

    /**
     * Helper method to show error dialogs
     */
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Gibt Zugriff auf ein bestimmtes Tab (für spätere View-Updates)
     */
    public Tab getTab(int index) {
        return tabPane.getTabs().get(index);
    }

    /**
     * Setzt den Inhalt eines bestimmten Tabs
     */
    public void setTabContent(int index, javafx.scene.Node content) {
        tabPane.getTabs().get(index).setContent(content);
    }
}