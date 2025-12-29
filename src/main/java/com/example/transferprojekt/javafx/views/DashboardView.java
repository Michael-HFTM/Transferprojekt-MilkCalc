package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.dataclasses.MilkDelivery;
import com.example.transferprojekt.services.AssignmentService;
import com.example.transferprojekt.services.MilkDeliveryService;
import com.example.transferprojekt.services.SupplierService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardView extends BorderPane {

    private final MilkDeliveryService milkDeliveryService;
    private final SupplierService supplierService;
    private final AssignmentService assignmentService;

    // Filter Controls
    private ComboBox<Company> supplierFilter;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private Button calculateButton;

    // Statistic Labels
    private Label deliveryCountLabel;
    private Label totalAmountLabel;
    private Label averageAmountLabel;

    public DashboardView(MilkDeliveryService milkDeliveryService,
                         SupplierService supplierService,
                         AssignmentService assignmentService) {
        this.milkDeliveryService = milkDeliveryService;
        this.supplierService = supplierService;
        this.assignmentService = assignmentService;

        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Dashboard - Statistiken");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox topBox = new VBox(10);
        topBox.getChildren().add(titleLabel);

        // Filter Section
        VBox filterSection = createFilterSection();

        // Statistics Section
        VBox statsSection = createStatisticsSection();

        // Layout
        VBox content = new VBox(30);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(topBox, filterSection, statsSection);

        setCenter(content);

        // Initialize with current month
        calculateStatistics();
    }

    private VBox createFilterSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label sectionTitle = new Label("Filter");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Supplier Filter
        HBox supplierBox = new HBox(10);
        supplierBox.setAlignment(Pos.CENTER_LEFT);
        Label supplierLabel = new Label("Lieferant:");
        supplierLabel.setPrefWidth(120);

        supplierFilter = new ComboBox<>();
        supplierFilter.setPromptText("Alle Lieferanten");
        supplierFilter.setPrefWidth(250);

        // Load suppliers
        try {
            List<Company> suppliers = supplierService.getDatabaseEntries();
            supplierFilter.getItems().add(null); // "Alle" option
            supplierFilter.getItems().addAll(suppliers);

            // Custom display
            supplierFilter.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Company item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Alle Lieferanten");
                    } else {
                        setText(item.getAddress().getName());
                    }
                }
            });

            supplierFilter.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Company item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Alle Lieferanten");
                    } else {
                        setText(item.getAddress().getName());
                    }
                }
            });

            supplierFilter.setValue(null); // Default: Alle

        } catch (Exception e) {
            showError("Fehler beim Laden der Lieferanten: " + e.getMessage());
        }

        supplierBox.getChildren().addAll(supplierLabel, supplierFilter);

        // Date Range Selection
        HBox dateRangeBox = new HBox(10);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        Label dateRangeLabel = new Label("Zeitraum:");
        dateRangeLabel.setPrefWidth(120);

        fromDatePicker = new DatePicker();
        fromDatePicker.setPromptText("Von");
        // Default: First day of current month
        LocalDate now = LocalDate.now();
        fromDatePicker.setValue(now.withDayOfMonth(1));

        Label toLabel = new Label("bis");

        toDatePicker = new DatePicker();
        toDatePicker.setPromptText("Bis");
        // Default: Last day of current month
        toDatePicker.setValue(now.withDayOfMonth(now.lengthOfMonth()));

        dateRangeBox.getChildren().addAll(dateRangeLabel, fromDatePicker, toLabel, toDatePicker);

        // Calculate Button
        calculateButton = new Button("Berechnen");
        calculateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        calculateButton.setOnAction(e -> calculateStatistics());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(calculateButton);

        section.getChildren().addAll(
                sectionTitle,
                supplierBox,
                dateRangeBox,
                buttonBox
        );

        return section;
    }

    private VBox createStatisticsSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(20));

        Label sectionTitle = new Label("Statistiken");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Statistics Cards
        HBox cardsBox = new HBox(20);
        cardsBox.setAlignment(Pos.CENTER);

        // Card 1: Anzahl Lieferungen
        VBox card1 = createStatCard("Anzahl Lieferungen", "0");
        deliveryCountLabel = (Label) card1.getChildren().get(1);

        // Card 2: Gesamte Menge
        VBox card2 = createStatCard("Gesamte Menge", "0.00 kg");
        totalAmountLabel = (Label) card2.getChildren().get(1);

        // Card 3: Durchschnittsmenge
        VBox card3 = createStatCard("Durchschnittsmenge", "0.00 kg");
        averageAmountLabel = (Label) card3.getChildren().get(1);

        cardsBox.getChildren().addAll(card1, card2, card3);

        section.getChildren().addAll(sectionTitle, cardsBox);

        return section;
    }

    private VBox createStatCard(String title, String initialValue) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        Label valueLabel = new Label(initialValue);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        card.getChildren().addAll(titleLabel, valueLabel);

        return card;
    }

    private void calculateStatistics() {
        try {
            // Get all deliveries
            List<MilkDelivery> allDeliveries = milkDeliveryService.getDatabaseEntries();

            // Filter by supplier if selected
            Company selectedSupplier = supplierFilter.getValue();
            if (selectedSupplier != null) {
                // Get all assignments for the selected supplier
                List<Assignment> allAssignments = assignmentService.getDatabaseEntries();

                // Find all supplier numbers assigned to this supplier
                List<Integer> supplierNumbersForSupplier = allAssignments.stream()
                        .filter(a -> a.getSupplierId().equals(selectedSupplier.getCompanyId()))
                        .map(a -> a.getSupplierNumber().getId())
                        .collect(Collectors.toList());

                // Filter deliveries by these supplier numbers
                allDeliveries = allDeliveries.stream()
                        .filter(d -> supplierNumbersForSupplier.contains(d.getSupplierNumber().getId()))
                        .collect(Collectors.toList());
            }

            // Filter by period
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();

            List<MilkDelivery> filteredDeliveries = allDeliveries.stream()
                    .filter(d -> !d.getDate().isBefore(fromDate) && !d.getDate().isAfter(toDate))
                    .collect(Collectors.toList());

            // Calculate statistics
            int count = filteredDeliveries.size();

            BigDecimal totalAmount = filteredDeliveries.stream()
                    .map(MilkDelivery::getAmountKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal averageAmount = count > 0
                    ? totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Update UI
            deliveryCountLabel.setText(String.valueOf(count));
            totalAmountLabel.setText(String.format("%.2f kg", totalAmount.doubleValue()));
            averageAmountLabel.setText(String.format("%.2f kg", averageAmount.doubleValue()));

        } catch (Exception e) {
            showError("Fehler bei der Berechnung: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText("Ein Fehler ist aufgetreten");
        alert.setContentText(message);
        alert.showAndWait();
    }
}