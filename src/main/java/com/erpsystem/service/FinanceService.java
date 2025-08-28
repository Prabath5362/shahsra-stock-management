package com.erpsystem.service;

import com.erpsystem.dao.PurchaseDAO;
import com.erpsystem.dao.SaleDAO;
import com.erpsystem.dao.CustomerDAO;
import com.erpsystem.dao.SupplierDAO;
import com.erpsystem.dao.ItemDAO;
import com.erpsystem.model.Purchase;
import com.erpsystem.model.Sale;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.erpsystem.util.DatabaseUtil;

/**
 * Service class for financial calculations and operations
 */
public class FinanceService {
    
    private final PurchaseDAO purchaseDAO;
    private final SaleDAO saleDAO;
    private final CustomerDAO customerDAO;
    private final SupplierDAO supplierDAO;
    private final ItemDAO itemDAO;
    
    public FinanceService() {
        this.purchaseDAO = new PurchaseDAO();
        this.saleDAO = new SaleDAO();
        this.customerDAO = new CustomerDAO();
        this.supplierDAO = new SupplierDAO();
        this.itemDAO = new ItemDAO();
    }
    
    /**
     * Calculate total money in (sales revenue) for all time
     * 
     * @return Total sales revenue
     * @throws SQLException if database operation fails
     */
    public BigDecimal getTotalMoneyIn() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_value), 0) FROM sales";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Calculate total money out (purchase costs) for all time
     * 
     * @return Total purchase costs
     * @throws SQLException if database operation fails
     */
    public BigDecimal getTotalMoneyOut() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_value), 0) FROM purchases";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Calculate total profit (sales - purchases)
     * 
     * @return Total profit
     * @throws SQLException if database operation fails
     */
    public BigDecimal getTotalProfit() throws SQLException {
        BigDecimal moneyIn = getTotalMoneyIn();
        BigDecimal moneyOut = getTotalMoneyOut();
        return moneyIn.subtract(moneyOut);
    }
    
    /**
     * Calculate current balance (money in - money out)
     * 
     * @return Current balance
     * @throws SQLException if database operation fails
     */
    public BigDecimal getCurrentBalance() throws SQLException {
        return getTotalProfit(); // Same as profit for now
    }
    
    /**
     * Get financial data for a specific date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return FinancialSummary for the date range
     * @throws SQLException if database operation fails
     */
    public FinancialSummary getFinancialSummary(LocalDate startDate, LocalDate endDate) throws SQLException {
        BigDecimal salesRevenue = getSalesRevenueByDateRange(startDate, endDate);
        BigDecimal purchaseCosts = getPurchaseCostsByDateRange(startDate, endDate);
        BigDecimal profit = salesRevenue.subtract(purchaseCosts);
        
        return new FinancialSummary(salesRevenue, purchaseCosts, profit, startDate, endDate);
    }
    
    /**
     * Get sales revenue for a date range
     */
    private BigDecimal getSalesRevenueByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_value), 0) FROM sales WHERE sale_date BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Get purchase costs for a date range
     */
    private BigDecimal getPurchaseCostsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_value), 0) FROM purchases WHERE purchase_date BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Get top customers by sales value
     * 
     * @param limit Number of top customers to return
     * @return List of CustomerSales data
     * @throws SQLException if database operation fails
     */
    public List<CustomerSales> getTopCustomers(int limit) throws SQLException {
        String sql = "SELECT c.customer_id, c.name, COALESCE(SUM(s.total_value), 0) as total_sales " +
                    "FROM customers c " +
                    "LEFT JOIN sales s ON c.customer_id = s.customer_id " +
                    "GROUP BY c.customer_id, c.name " +
                    "ORDER BY total_sales DESC " +
                    "LIMIT ?";
        
        List<CustomerSales> topCustomers = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topCustomers.add(new CustomerSales(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("total_sales")
                    ));
                }
            }
        }
        
        return topCustomers;
    }
    
    /**
     * Get top items by sales quantity
     * 
     * @param limit Number of top items to return
     * @return List of ItemSales data
     * @throws SQLException if database operation fails
     */
    public List<ItemSales> getTopItems(int limit) throws SQLException {
        String sql = "SELECT i.item_id, i.name, COALESCE(SUM(s.quantity), 0) as total_quantity, " +
                    "COALESCE(SUM(s.total_value), 0) as total_value " +
                    "FROM items i " +
                    "LEFT JOIN sales s ON i.item_id = s.item_id " +
                    "GROUP BY i.item_id, i.name " +
                    "ORDER BY total_quantity DESC " +
                    "LIMIT ?";
        
        List<ItemSales> topItems = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topItems.add(new ItemSales(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getInt("total_quantity"),
                        rs.getBigDecimal("total_value")
                    ));
                }
            }
        }
        
        return topItems;
    }
    
    /**
     * Get monthly sales and purchase data for charts
     * 
     * @param year The year to get data for
     * @return Map of month names to MonthlyData
     * @throws SQLException if database operation fails
     */
    public Map<String, MonthlyData> getMonthlyData(int year) throws SQLException {
        Map<String, MonthlyData> monthlyData = new HashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        // Initialize all months with zero values
        for (int i = 0; i < 12; i++) {
            monthlyData.put(months[i], new MonthlyData(BigDecimal.ZERO, BigDecimal.ZERO));
        }
        
        // Get sales data by month
        String salesSql = "SELECT strftime('%m', sale_date) as month, SUM(total_value) as total " +
                         "FROM sales WHERE strftime('%Y', sale_date) = ? GROUP BY month";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(salesSql)) {
            
            stmt.setString(1, String.valueOf(year));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int monthNum = rs.getInt("month") - 1; // Convert to 0-based index
                    if (monthNum >= 0 && monthNum < 12) {
                        String monthName = months[monthNum];
                        MonthlyData data = monthlyData.get(monthName);
                        monthlyData.put(monthName, new MonthlyData(rs.getBigDecimal("total"), data.getPurchases()));
                    }
                }
            }
        }
        
        // Get purchases data by month
        String purchasesSql = "SELECT strftime('%m', purchase_date) as month, SUM(total_value) as total " +
                             "FROM purchases WHERE strftime('%Y', purchase_date) = ? GROUP BY month";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(purchasesSql)) {
            
            stmt.setString(1, String.valueOf(year));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int monthNum = rs.getInt("month") - 1; // Convert to 0-based index
                    if (monthNum >= 0 && monthNum < 12) {
                        String monthName = months[monthNum];
                        MonthlyData data = monthlyData.get(monthName);
                        monthlyData.put(monthName, new MonthlyData(data.getSales(), rs.getBigDecimal("total")));
                    }
                }
            }
        }
        
        return monthlyData;
    }
    
    // Inner classes for data transfer
    
    public static class FinancialSummary {
        private final BigDecimal salesRevenue;
        private final BigDecimal purchaseCosts;
        private final BigDecimal profit;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public FinancialSummary(BigDecimal salesRevenue, BigDecimal purchaseCosts, 
                               BigDecimal profit, LocalDate startDate, LocalDate endDate) {
            this.salesRevenue = salesRevenue;
            this.purchaseCosts = purchaseCosts;
            this.profit = profit;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters
        public BigDecimal getSalesRevenue() { return salesRevenue; }
        public BigDecimal getPurchaseCosts() { return purchaseCosts; }
        public BigDecimal getProfit() { return profit; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }
    
    public static class CustomerSales {
        private final int customerId;
        private final String customerName;
        private final BigDecimal totalSales;
        
        public CustomerSales(int customerId, String customerName, BigDecimal totalSales) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.totalSales = totalSales;
        }
        
        // Getters
        public int getCustomerId() { return customerId; }
        public String getCustomerName() { return customerName; }
        public BigDecimal getTotalSales() { return totalSales; }
    }
    
    public static class ItemSales {
        private final int itemId;
        private final String itemName;
        private final int totalQuantity;
        private final BigDecimal totalValue;
        
        public ItemSales(int itemId, String itemName, int totalQuantity, BigDecimal totalValue) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.totalQuantity = totalQuantity;
            this.totalValue = totalValue;
        }
        
        // Getters
        public int getItemId() { return itemId; }
        public String getItemName() { return itemName; }
        public int getTotalQuantity() { return totalQuantity; }
        public BigDecimal getTotalValue() { return totalValue; }
    }
    
    public static class MonthlyData {
        private final BigDecimal sales;
        private final BigDecimal purchases;
        
        public MonthlyData(BigDecimal sales, BigDecimal purchases) {
            this.sales = sales;
            this.purchases = purchases;
        }
        
        // Getters
        public BigDecimal getSales() { return sales; }
        public BigDecimal getPurchases() { return purchases; }
        public BigDecimal getProfit() { return sales.subtract(purchases); }
    }
}