package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.services.SupplierService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {

    private final TabPane tabPane;
    private final MenuBar menuBar;
    private final SupplierService supplierService;

    public MainView(SupplierService supplierService) {
        this.supplierService = supplierService;

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

        // Tab 1: Lieferanten (with real SupplierView!)
        Tab supplierTab = new Tab("Lieferanten");
        supplierTab.setContent(new SupplierView(supplierService));

        // Tab 2: Zuweisungen
        Tab assignmentTab = new Tab("Zuweisungen");
        assignmentTab.setContent(createPlaceholderView("Zuweisungen-Verwaltung"));

        // Tab 3: Milchlieferungen
        Tab deliveryTab = new Tab("Milchlieferungen");
        deliveryTab.setContent(createPlaceholderView("Milchlieferungen-Verwaltung"));

        // Tab 4: Übersicht/Dashboard (optional)
        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setContent(createPlaceholderView("Dashboard"));

        tabPane.getTabs().addAll(dashboardTab, supplierTab, assignmentTab, deliveryTab);

        return tabPane;
    }

    /**
     * Erstellt eine Platzhalter-Ansicht für Tabs, die noch nicht implementiert sind
     */
    private VBox createPlaceholderView(String title) {
        VBox placeholder = new VBox(20);
        placeholder.setPadding(new Insets(40));
        placeholder.setStyle("-fx-alignment: center;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Diese Ansicht wird in den nächsten Schritten implementiert.");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        placeholder.getChildren().addAll(titleLabel, infoLabel);

        return placeholder;
    }

    /**
     * Aktualisiert alle Views (wird später mit echten Services verbunden)
     */
    private void refreshAllViews() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aktualisieren");
        alert.setHeaderText(null);
        alert.setContentText("Alle Ansichten werden aktualisiert...");
        alert.showAndWait();

        // TODO: Später mit echten Service-Aufrufen ersetzen
        System.out.println("Refreshing all views...");
    }

    /**
     * Fügt Testdaten ein (wird später mit TestdataService verbunden)
     */
    private void insertTestData() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Testdaten einfügen");
        confirmation.setHeaderText("Testdaten einfügen?");
        confirmation.setContentText("Dies wird alle vorhandenen Daten löschen und Testdaten einfügen.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: TestdataService aufrufen
                System.out.println("Inserting test data...");

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Erfolg");
                success.setHeaderText(null);
                success.setContentText("Testdaten wurden erfolgreich eingefügt!");
                success.showAndWait();
            }
        });
    }

    /**
     * Löscht alle Daten (wird später mit AdminToolsService verbunden)
     */
    private void clearAllData() {
        Alert confirmation = new Alert(Alert.AlertType.WARNING);
        confirmation.setTitle("Alle Daten löschen");
        confirmation.setHeaderText("WARNUNG: Alle Daten löschen?");
        confirmation.setContentText("Diese Aktion kann nicht rückgängig gemacht werden!");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Zweite Bestätigung für kritische Aktion
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Bestätigung");
                dialog.setHeaderText("Geben Sie 'DELETE' ein, um zu bestätigen:");
                dialog.setContentText("Bestätigung:");

                dialog.showAndWait().ifPresent(input -> {
                    if ("DELETE".equals(input)) {
                        // TODO: AdminToolsService aufrufen
                        System.out.println("Deleting all data...");

                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Erfolg");
                        success.setHeaderText(null);
                        success.setContentText("Alle Daten wurden gelöscht!");
                        success.showAndWait();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Abgebrochen");
                        error.setHeaderText(null);
                        error.setContentText("Falsche Eingabe. Aktion abgebrochen.");
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