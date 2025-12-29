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

    // Statistic Labels - All Suppliers
    private Label allDeliveryCountValue;
    private Label allTotalAmountValue;
    private Label allAverageAmountValue;

    // Statistic Labels - Selected Supplier
    private Label selectedDeliveryCountValue;
    private Label selectedTotalAmountValue;
    private Label selectedAverageAmountValue;
    private VBox selectedSupplierSection;

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

        // Statistics Section - Both side by side
        HBox statsContainer = createStatisticsContainer();

        // Layout
        VBox content = new VBox(30);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(topBox, filterSection, statsContainer);

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

    private HBox createStatisticsContainer() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.TOP_LEFT);

        // Left: All Suppliers Statistics (always visible)
        VBox allStatsSection = createAllSuppliersStatisticsSection();
        HBox.setHgrow(allStatsSection, Priority.ALWAYS);

        // Right: Selected Supplier Statistics (conditionally visible)
        selectedSupplierSection = createSelectedSupplierStatisticsSection();
        selectedSupplierSection.setVisible(false);
        selectedSupplierSection.setManaged(false);
        HBox.setHgrow(selectedSupplierSection, Priority.ALWAYS);

        container.getChildren().addAll(allStatsSection, selectedSupplierSection);

        return container;
    }

    private VBox createAllSuppliersStatisticsSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        section.setMinWidth(350);

        Label sectionTitle = new Label("Statistiken - Alle Lieferanten");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Statistics Grid - left-aligned with labels and values
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(10, 0, 0, 0));

        // Row 1: Anzahl Lieferungen
        Label deliveryCountLabel = new Label("Anzahl Lieferungen:");
        deliveryCountLabel.setStyle("-fx-font-size: 14px;");
        allDeliveryCountValue = new Label("0");
        allDeliveryCountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(deliveryCountLabel, 0, 0);
        statsGrid.add(allDeliveryCountValue, 1, 0);

        // Row 2: Gesamte Menge
        Label totalAmountLabel = new Label("Gesamte Menge:");
        totalAmountLabel.setStyle("-fx-font-size: 14px;");
        allTotalAmountValue = new Label("0.00 kg");
        allTotalAmountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(totalAmountLabel, 0, 1);
        statsGrid.add(allTotalAmountValue, 1, 1);

        // Row 3: Durchschnittsmenge
        Label averageAmountLabel = new Label("Durchschnittsmenge:");
        averageAmountLabel.setStyle("-fx-font-size: 14px;");
        allAverageAmountValue = new Label("0.00 kg");
        allAverageAmountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(averageAmountLabel, 0, 2);
        statsGrid.add(allAverageAmountValue, 1, 2);

        // Column constraints to ensure proper alignment
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(180);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(100);
        statsGrid.getColumnConstraints().addAll(col1, col2);

        section.getChildren().addAll(sectionTitle, statsGrid);

        return section;
    }

    private VBox createSelectedSupplierStatisticsSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        section.setMinWidth(350);

        Label sectionTitle = new Label("Statistiken - Ausgewählter Lieferant");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Statistics Grid - left-aligned with labels and values
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(10, 0, 0, 0));

        // Row 1: Anzahl Lieferungen
        Label deliveryCountLabel = new Label("Anzahl Lieferungen:");
        deliveryCountLabel.setStyle("-fx-font-size: 14px;");
        selectedDeliveryCountValue = new Label("0");
        selectedDeliveryCountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(deliveryCountLabel, 0, 0);
        statsGrid.add(selectedDeliveryCountValue, 1, 0);

        // Row 2: Gesamte Menge
        Label totalAmountLabel = new Label("Gesamte Menge:");
        totalAmountLabel.setStyle("-fx-font-size: 14px;");
        selectedTotalAmountValue = new Label("0.00 kg");
        selectedTotalAmountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(totalAmountLabel, 0, 1);
        statsGrid.add(selectedTotalAmountValue, 1, 1);

        // Row 3: Durchschnittsmenge
        Label averageAmountLabel = new Label("Durchschnittsmenge:");
        averageAmountLabel.setStyle("-fx-font-size: 14px;");
        selectedAverageAmountValue = new Label("0.00 kg");
        selectedAverageAmountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(averageAmountLabel, 0, 2);
        statsGrid.add(selectedAverageAmountValue, 1, 2);

        // Column constraints to ensure proper alignment
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(180);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(100);
        statsGrid.getColumnConstraints().addAll(col1, col2);

        section.getChildren().addAll(sectionTitle, statsGrid);

        return section;
    }

    private void calculateStatistics() {
        try {
            // Filter by period first
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();

            // Get all deliveries
            List<MilkDelivery> allDeliveries = milkDeliveryService.getDatabaseEntries();

            // Filter by period for "all suppliers" statistics
            List<MilkDelivery> allFilteredDeliveries = allDeliveries.stream()
                    .filter(d -> !d.getDate().isBefore(fromDate) && !d.getDate().isAfter(toDate))
                    .collect(Collectors.toList());

            // Calculate statistics for all suppliers
            updateStatistics(
                    allFilteredDeliveries,
                    allDeliveryCountValue,
                    allTotalAmountValue,
                    allAverageAmountValue
            );

            // Check if a specific supplier is selected
            Company selectedSupplier = supplierFilter.getValue();
            if (selectedSupplier != null) {
                // Show selected supplier section
                selectedSupplierSection.setVisible(true);
                selectedSupplierSection.setManaged(true);

                // Get all assignments for the selected supplier
                List<Assignment> allAssignments = assignmentService.getDatabaseEntries();

                // Find all supplier numbers assigned to this supplier
                List<Integer> supplierNumbersForSupplier = allAssignments.stream()
                        .filter(a -> a.getSupplierId().equals(selectedSupplier.getCompanyId()))
                        .map(a -> a.getSupplierNumber().getId())
                        .collect(Collectors.toList());

                // Filter deliveries by supplier numbers and period
                List<MilkDelivery> selectedFilteredDeliveries = allDeliveries.stream()
                        .filter(d -> supplierNumbersForSupplier.contains(d.getSupplierNumber().getId()))
                        .filter(d -> !d.getDate().isBefore(fromDate) && !d.getDate().isAfter(toDate))
                        .collect(Collectors.toList());

                // Calculate statistics for selected supplier
                updateStatistics(
                        selectedFilteredDeliveries,
                        selectedDeliveryCountValue,
                        selectedTotalAmountValue,
                        selectedAverageAmountValue
                );
            } else {
                // Hide selected supplier section when "Alle Lieferanten" is selected
                selectedSupplierSection.setVisible(false);
                selectedSupplierSection.setManaged(false);
            }

        } catch (Exception e) {
            showError("Fehler bei der Berechnung: " + e.getMessage());
        }
    }

    private void updateStatistics(List<MilkDelivery> deliveries,
                                  Label countLabel,
                                  Label totalLabel,
                                  Label averageLabel) {
        int count = deliveries.size();

        BigDecimal totalAmount = deliveries.stream()
                .map(MilkDelivery::getAmountKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageAmount = count > 0
                ? totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Update UI
        countLabel.setText(String.valueOf(count));
        totalLabel.setText(String.format("%.2f kg", totalAmount.doubleValue()));
        averageLabel.setText(String.format("%.2f kg", averageAmount.doubleValue()));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText("Ein Fehler ist aufgetreten");
        alert.setContentText(message);
        alert.showAndWait();
    }
}