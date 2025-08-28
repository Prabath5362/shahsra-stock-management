package com.erpsystem.dao;

import com.erpsystem.model.Sale;
import com.erpsystem.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Sale entities
 */
public class SaleDAO implements BaseDAO<Sale, Integer> {
    
    private static final String INSERT_SQL = 
        "INSERT INTO sales (item_id, quantity, customer_id, sales_rate, total_value, sale_date) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE sales SET item_id = ?, quantity = ?, customer_id = ?, sales_rate = ?, " +
        "total_value = ?, sale_date = ? WHERE sale_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM sales WHERE sale_id = ?";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT s.*, i.name as item_name, c.name as customer_name " +
        "FROM sales s " +
        "LEFT JOIN items i ON s.item_id = i.item_id " +
        "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
        "WHERE s.sale_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT s.*, i.name as item_name, c.name as customer_name " +
        "FROM sales s " +
        "LEFT JOIN items i ON s.item_id = i.item_id " +
        "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
        "ORDER BY s.sale_date DESC, s.created_date DESC";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM sales";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM sales WHERE sale_id = ? LIMIT 1";
    
    @Override
    public Integer insert(Sale sale) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, sale.getItemId());
            stmt.setInt(2, sale.getQuantity());
            stmt.setInt(3, sale.getCustomerId());
            stmt.setBigDecimal(4, sale.getSalesRate());
            stmt.setBigDecimal(5, sale.getTotalValue());
            stmt.setDate(6, Date.valueOf(sale.getSaleDate()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating sale failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    sale.setSaleId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating sale failed, no ID obtained.");
                }
            }
        }
    }
    
    @Override
    public boolean update(Sale sale) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setInt(1, sale.getItemId());
            stmt.setInt(2, sale.getQuantity());
            stmt.setInt(3, sale.getCustomerId());
            stmt.setBigDecimal(4, sale.getSalesRate());
            stmt.setBigDecimal(5, sale.getTotalValue());
            stmt.setDate(6, Date.valueOf(sale.getSaleDate()));
            stmt.setInt(7, sale.getSaleId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public boolean delete(Integer id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public Optional<Sale> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSale(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public List<Sale> findAll() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        }
        
        return sales;
    }
    
    @Override
    public long count() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }
    
    @Override
    public boolean exists(Integer id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Find sales by item ID
     */
    public List<Sale> findByItemId(int itemId) throws SQLException {
        String sql = "SELECT s.*, i.name as item_name, c.name as customer_name " +
                    "FROM sales s " +
                    "LEFT JOIN items i ON s.item_id = i.item_id " +
                    "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                    "WHERE s.item_id = ? ORDER BY s.sale_date DESC";
        
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapResultSetToSale(rs));
                }
            }
        }
        
        return sales;
    }
    
    /**
     * Find sales by customer ID
     */
    public List<Sale> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT s.*, i.name as item_name, c.name as customer_name " +
                    "FROM sales s " +
                    "LEFT JOIN items i ON s.item_id = i.item_id " +
                    "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                    "WHERE s.customer_id = ? ORDER BY s.sale_date DESC";
        
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapResultSetToSale(rs));
                }
            }
        }
        
        return sales;
    }
    
    /**
     * Find sales by date range
     */
    public List<Sale> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT s.*, i.name as item_name, c.name as customer_name " +
                    "FROM sales s " +
                    "LEFT JOIN items i ON s.item_id = i.item_id " +
                    "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                    "WHERE s.sale_date BETWEEN ? AND ? ORDER BY s.sale_date DESC";
        
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapResultSetToSale(rs));
                }
            }
        }
        
        return sales;
    }
    
    /**
     * Get total sales quantity for an item
     */
    public int getTotalQuantityByItemId(int itemId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE item_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
     * Map ResultSet to Sale object
     */
    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));
        sale.setItemId(rs.getInt("item_id"));
        sale.setQuantity(rs.getInt("quantity"));
        sale.setCustomerId(rs.getInt("customer_id"));
        sale.setSalesRate(rs.getBigDecimal("sales_rate"));
        sale.setTotalValue(rs.getBigDecimal("total_value"));
        sale.setSaleDate(rs.getDate("sale_date").toLocalDate());
        
        Timestamp createdTimestamp = rs.getTimestamp("created_date");
        if (createdTimestamp != null) {
            sale.setCreatedDate(createdTimestamp.toLocalDateTime());
        }
        
        // Set display names if available
        sale.setItemName(rs.getString("item_name"));
        sale.setCustomerName(rs.getString("customer_name"));
        
        return sale;
    }
}