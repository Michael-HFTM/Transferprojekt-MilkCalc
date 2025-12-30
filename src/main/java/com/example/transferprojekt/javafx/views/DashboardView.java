package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.dataclasses.MilkDelivery;
import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
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
    private Label selectedSupplierTitle;
    private Label selectedDeliveryCountValue;
    private Label selectedTotalAmountValue;
    private Label selectedAverageAmountValue;
    private VBox selectedSupplierSection;

    // Cached data for local filtering
    private List<MilkDelivery> allDeliveries;
    private List<Assignment> allAssignments;

    public DashboardView(MilkDeliveryService milkDeliveryService,
                         SupplierService supplierService,
                         AssignmentService assignmentService) {
        this.milkDeliveryService = milkDeliveryService;
        this.supplierService = supplierService;
        this.assignmentService = assignmentService;

        initializeUI();
        loadDataAndCalculate();
    }

    private void initializeUI() {
        setPadding(new Insets(20));

        Label titleLabel = new Label("Dashboard - Statistiken");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox topBox = new VBox(10);
        topBox.getChildren().add(titleLabel);

        VBox filterSection = createFilterSection();
        HBox statsContainer = createStatisticsContainer();

        VBox content = new VBox(30);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(topBox, filterSection, statsContainer);

        setCenter(content);
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

        // Load suppliers async
        AsyncDatabaseTask.run(
                () -> supplierService.getDatabaseEntries(),
                this,
                suppliers -> {
                    supplierFilter.getItems().add(null); // "Alle" option
                    supplierFilter.getItems().addAll(suppliers);

                    supplierFilter.setCellFactory(param -> new ListCell<>() {
                        @Override
                        protected void updateItem(Company item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? "Alle Lieferanten" : item.getAddress().getName());
                        }
                    });

                    supplierFilter.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(Company item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? "Alle Lieferanten" : item.getAddress().getName());
                        }
                    });

                    supplierFilter.setValue(null);
                },
                error -> showError("Fehler beim Laden der Lieferanten: " + error.getMessage())
        );

        supplierBox.getChildren().addAll(supplierLabel, supplierFilter);

        // Date Range Selection
        HBox dateRangeBox = new HBox(10);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        Label dateRangeLabel = new Label("Zeitraum:");
        dateRangeLabel.setPrefWidth(120);

        fromDatePicker = new DatePicker();
        fromDatePicker.setPromptText("Von");
        LocalDate now = LocalDate.now();
        fromDatePicker.setValue(now.withMonth(1).withDayOfMonth(1));

        Label toLabel = new Label("bis");

        toDatePicker = new DatePicker();
        toDatePicker.setPromptText("Bis");
        toDatePicker.setValue(now.withMonth(12).withDayOfMonth(31));

        dateRangeBox.getChildren().addAll(dateRangeLabel, fromDatePicker, toLabel, toDatePicker);

        // Calculate Button
        calculateButton = new Button("Berechnen");
        calculateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        calculateButton.setOnAction(e -> calculateStatistics());

        // Refresh Button
        Button refreshButton = new Button("Aktualisieren");
        refreshButton.setOnAction(e -> loadDataAndCalculate());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().addAll(calculateButton, refreshButton);

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

        VBox allStatsSection = createAllSuppliersStatisticsSection();
        HBox.setHgrow(allStatsSection, Priority.ALWAYS);

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

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(10, 0, 0, 0));

        Label deliveryCountLabel = new Label("Anzahl Lieferungen:");
        deliveryCountLabel.setStyle("-fx-font-size: 14px;");
        allDeliveryCountValue = new Label("0");
        allDeliveryCountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(deliveryCountLabel, 0, 0);
        statsGrid.add(allDeliveryCountValue, 1, 0);

        Label totalAmountLabel = new Label("Gesamte Menge:");
        totalAmountLabel.setStyle("-fx-font-size: 14px;");
        allTotalAmountValue = new Label("0.00 kg");
        allTotalAmountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(totalAmountLabel, 0, 1);
        statsGrid.add(allTotalAmountValue, 1, 1);

        Label averageAmountLabel = new Label("Durchschnittsmenge:");
        averageAmountLabel.setStyle("-fx-font-size: 14px;");
        allAverageAmountValue = new Label("0.00 kg");
        allAverageAmountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(averageAmountLabel, 0, 2);
        statsGrid.add(allAverageAmountValue, 1, 2);

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

        selectedSupplierTitle = new Label();
        selectedSupplierTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(10, 0, 0, 0));

        Label deliveryCountLabel = new Label("Anzahl Lieferungen:");
        deliveryCountLabel.setStyle("-fx-font-size: 14px;");
        selectedDeliveryCountValue = new Label("0");
        selectedDeliveryCountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(deliveryCountLabel, 0, 0);
        statsGrid.add(selectedDeliveryCountValue, 1, 0);

        Label totalAmountLabel = new Label("Gesamte Menge:");
        totalAmountLabel.setStyle("-fx-font-size: 14px;");
        selectedTotalAmountValue = new Label("0.00 kg");
        selectedTotalAmountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(totalAmountLabel, 0, 1);
        statsGrid.add(selectedTotalAmountValue, 1, 1);

        Label averageAmountLabel = new Label("Durchschnittsmenge:");
        averageAmountLabel.setStyle("-fx-font-size: 14px;");
        selectedAverageAmountValue = new Label("0.00 kg");
        selectedAverageAmountValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(averageAmountLabel, 0, 2);
        statsGrid.add(selectedAverageAmountValue, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(180);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(100);
        statsGrid.getColumnConstraints().addAll(col1, col2);

        section.getChildren().addAll(selectedSupplierTitle, statsGrid);

        return section;
    }

    /**
     * Public method to refresh dashboard data
     * Called when dashboard tab is selected
     */
    public void refresh() {
        loadDataAndCalculate();
    }

    /**
     * Initial data load (ASYNC)
     */
    private void loadDataAndCalculate() {
        calculateButton.setDisable(true);

        AsyncDatabaseTask.run(
                () -> {
                    // Load all data in one go
                    List<MilkDelivery> deliveries = milkDeliveryService.getDatabaseEntries();
                    List<Assignment> assignments = assignmentService.getDatabaseEntries();
                    return new Object[] { deliveries, assignments };
                },
                this,
                result -> {
                    allDeliveries = (List<MilkDelivery>) result[0];
                    allAssignments = (List<Assignment>) result[1];
                    calculateButton.setDisable(false);
                    calculateStatistics(); // Calculate with loaded data
                },
                error -> {
                    showError("Fehler beim Laden der Daten: " + error.getMessage());
                    calculateButton.setDisable(false);
                }
        );
    }

    /**
     * Calculate statistics (LOCAL - uses cached data)
     */
    private void calculateStatistics() {
        if (allDeliveries == null || allAssignments == null) {
            return;
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        // Filter by period - LOCAL operation
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
            selectedSupplierSection.setVisible(true);
            selectedSupplierSection.setManaged(true);

            selectedSupplierTitle.setText("Statistiken - " + selectedSupplier.getAddress().getName());

            // Find all supplier numbers assigned to this supplier - LOCAL
            List<Integer> supplierNumbersForSupplier = allAssignments.stream()
                    .filter(a -> a.getSupplierId().equals(selectedSupplier.getCompanyId()))
                    .map(a -> a.getSupplierNumber().getId())
                    .collect(Collectors.toList());

            // Filter deliveries by supplier numbers and period - LOCAL
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
            selectedSupplierSection.setVisible(false);
            selectedSupplierSection.setManaged(false);
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