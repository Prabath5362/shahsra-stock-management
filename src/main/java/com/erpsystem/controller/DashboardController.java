package com.erpsystem.controller;

import com.erpsystem.service.FinanceService;
import com.erpsystem.service.InventoryService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Dashboard view
 */
public class DashboardController implements Initializable {
    
    // Services
    private final FinanceService financeService = new FinanceService();
    private final InventoryService inventoryService = new InventoryService();
    
    // Key Metrics Labels
    @FXML private Label totalSalesLabel;
    @FXML private Label totalPurchasesLabel;
    @FXML private Label totalProfitLabel;
    @FXML private Label inventoryValueLabel;
    
    // Inventory Summary Labels
    @FXML private Label totalItemsLabel;
    @FXML private Label inStockItemsLabel;
    @FXML private Label lowStockItemsLabel;
    @FXML private Label outOfStockItemsLabel;
    
    // Charts
    @FXML private BarChart<String, Number> monthlyChart;
    @FXML private CategoryAxis monthlyXAxis;
    @FXML private NumberAxis monthlyYAxis;
    @FXML private PieChart revenueChart;
    
    // Tables
    @FXML private TableView<FinanceService.CustomerSales> topCustomersTable;
    @FXML private TableColumn<FinanceService.CustomerSales, String> customerNameColumn;
    @FXML private TableColumn<FinanceService.CustomerSales, String> customerSalesColumn;
    
    @FXML private TableView<FinanceService.ItemSales> topItemsTable;
    @FXML private TableColumn<FinanceService.ItemSales, String> itemNameColumn;
    @FXML private TableColumn<FinanceService.ItemSales, Integer> itemQuantityColumn;
    @FXML private TableColumn<FinanceService.ItemSales, String> itemValueColumn;
    
    // Action Buttons
    @FXML private Button addPurchaseBtn;
    @FXML private Button addSaleBtn;
    @FXML private Button viewInventoryBtn;
    @FXML private Button refreshBtn;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        setupCharts();
        loadDashboardData();
    }
    
    /**
     * Setup table columns with proper cell value factories
     */
    private void setupTableColumns() {
        // Top Customers Table
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerSalesColumn.setCellValueFactory(cellData -> {
            BigDecimal value = cellData.getValue().getTotalSales();
            return new javafx.beans.property.SimpleStringProperty("$" + String.format("%.2f", value.doubleValue()));
        });
        
        // Top Items Table
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));
        itemValueColumn.setCellValueFactory(cellData -> {
            BigDecimal value = cellData.getValue().getTotalValue();
            return new javafx.beans.property.SimpleStringProperty("$" + String.format("%.2f", value.doubleValue()));
        });
    }
    
    /**
     * Setup chart properties
     */
    private void setupCharts() {
        // Monthly Chart
        monthlyChart.setTitle("Monthly Revenue vs Costs");
        monthlyXAxis.setLabel("Month");
        monthlyYAxis.setLabel("Amount ($)");
        
        // Revenue Chart
        revenueChart.setTitle("Revenue vs Costs vs Profit");
    }
    
    /**
     * Load all dashboard data asynchronously
     */
    private void loadDashboardData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load financial metrics
                loadFinancialMetrics();
                
                // Load inventory summary
                loadInventorySummary();
                
                // Load charts data
                loadChartsData();
                
                // Load tables data
                loadTablesData();
                
                return null;
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            // Task completed successfully
        });
        
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            System.err.println("Error loading dashboard data: " + exception.getMessage());
            exception.printStackTrace();
        });
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Load financial metrics
     */
    private void loadFinancialMetrics() {
        try {
            BigDecimal totalSales = financeService.getTotalMoneyIn();
            BigDecimal totalPurchases = financeService.getTotalMoneyOut();
            BigDecimal totalProfit = financeService.getTotalProfit();
            BigDecimal inventoryValue = inventoryService.getTotalInventorySalesValue();
            
            Platform.runLater(() -> {
                totalSalesLabel.setText("$" + String.format("%.2f", totalSales.doubleValue()));
                totalPurchasesLabel.setText("$" + String.format("%.2f", totalPurchases.doubleValue()));
                totalProfitLabel.setText("$" + String.format("%.2f", totalProfit.doubleValue()));
                inventoryValueLabel.setText("$" + String.format("%.2f", inventoryValue.doubleValue()));
            });
            
        } catch (SQLException e) {
            System.err.println("Database error loading financial metrics: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error loading financial metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load inventory summary
     */
    private void loadInventorySummary() {
        try {
            InventoryService.InventorySummary summary = inventoryService.getInventorySummary();
            
            Platform.runLater(() -> {
                totalItemsLabel.setText(String.valueOf(summary.getTotalItems()));
                inStockItemsLabel.setText(String.valueOf(summary.getInStockItems()));
                lowStockItemsLabel.setText(String.valueOf(summary.getLowStockItems()));
                outOfStockItemsLabel.setText(String.valueOf(summary.getOutOfStockItems()));
            });
            
        } catch (SQLException e) {
            System.err.println("Database error loading inventory summary: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error loading inventory summary: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load charts data
     */
    private void loadChartsData() {
        try {
            // Load monthly data for current year
            int currentYear = LocalDate.now().getYear();
            Map<String, FinanceService.MonthlyData> monthlyData = financeService.getMonthlyData(currentYear);
            
            // Load financial data for pie chart outside of Platform.runLater
            BigDecimal totalSales = financeService.getTotalMoneyIn();
            BigDecimal totalPurchases = financeService.getTotalMoneyOut();
            BigDecimal totalProfit = totalSales.subtract(totalPurchases);
            
            Platform.runLater(() -> {
                // Setup monthly chart
                XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
                salesSeries.setName("Sales");
                
                XYChart.Series<String, Number> purchasesSeries = new XYChart.Series<>();
                purchasesSeries.setName("Purchases");
                
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                
                for (String month : months) {
                    FinanceService.MonthlyData data = monthlyData.getOrDefault(month, 
                        new FinanceService.MonthlyData(BigDecimal.ZERO, BigDecimal.ZERO));
                    
                    salesSeries.getData().add(new XYChart.Data<>(month, data.getSales().doubleValue()));
                    purchasesSeries.getData().add(new XYChart.Data<>(month, data.getPurchases().doubleValue()));
                }
                
                monthlyChart.getData().clear();
                monthlyChart.getData().addAll(salesSeries, purchasesSeries);
                
                // Setup revenue pie chart
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Sales Revenue", totalSales.doubleValue()),
                    new PieChart.Data("Purchase Costs", totalPurchases.doubleValue()),
                    new PieChart.Data("Profit", totalProfit.doubleValue())
                );
                
                revenueChart.setData(pieChartData);
            });
            
        } catch (SQLException e) {
            System.err.println("Database error loading charts data: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error loading charts data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load tables data
     */
    private void loadTablesData() {
        try {
            List<FinanceService.CustomerSales> topCustomers = financeService.getTopCustomers(5);
            List<FinanceService.ItemSales> topItems = financeService.getTopItems(5);
            
            Platform.runLater(() -> {
                topCustomersTable.setItems(FXCollections.observableList(topCustomers));
                topItemsTable.setItems(FXCollections.observableList(topItems));
            });
            
        } catch (SQLException e) {
            System.err.println("Database error loading tables data: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error loading tables data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Action handlers
    
    @FXML
    private void addPurchase() {
        // Navigate to purchasing module
        // TODO: Implement navigation to purchasing view
        System.out.println("Navigate to purchasing module");
    }
    
    @FXML
    private void addSale() {
        // Navigate to sales module
        // TODO: Implement navigation to sales view
        System.out.println("Navigate to sales module");
    }
    
    @FXML
    private void viewInventory() {
        // Navigate to inventory module
        // TODO: Implement navigation to inventory view
        System.out.println("Navigate to inventory module");
    }
    
    @FXML
    private void refreshData() {
        loadDashboardData();
    }
}