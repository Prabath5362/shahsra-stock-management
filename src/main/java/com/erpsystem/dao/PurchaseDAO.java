package com.erpsystem.dao;

import com.erpsystem.model.Purchase;
import com.erpsystem.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Purchase entities
 */
public class PurchaseDAO implements BaseDAO<Purchase, Integer> {
    
    private static final String INSERT_SQL = 
        "INSERT INTO purchases (item_id, quantity, supplier_id, purchase_rate, total_value, purchase_date) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE purchases SET item_id = ?, quantity = ?, supplier_id = ?, purchase_rate = ?, " +
        "total_value = ?, purchase_date = ? WHERE purchase_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM purchases WHERE purchase_id = ?";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT p.*, i.name as item_name, s.name as supplier_name " +
        "FROM purchases p " +
        "LEFT JOIN items i ON p.item_id = i.item_id " +
        "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
        "WHERE p.purchase_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT p.*, i.name as item_name, s.name as supplier_name " +
        "FROM purchases p " +
        "LEFT JOIN items i ON p.item_id = i.item_id " +
        "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
        "ORDER BY p.purchase_date DESC, p.created_date DESC";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM purchases";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM purchases WHERE purchase_id = ? LIMIT 1";
    
    @Override
    public Integer insert(Purchase purchase) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement idStmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            stmt = conn.prepareStatement(INSERT_SQL);
            stmt.setInt(1, purchase.getItemId());
            stmt.setInt(2, purchase.getQuantity());
            stmt.setInt(3, purchase.getSupplierId());
            stmt.setBigDecimal(4, purchase.getPurchaseRate());
            stmt.setBigDecimal(5, purchase.getTotalValue());
            stmt.setString(6, purchase.getPurchaseDate().toString());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating purchase failed, no rows affected.");
            }
            
            // Get the last inserted ID
            idStmt = conn.prepareStatement("SELECT last_insert_rowid()");
            rs = idStmt.executeQuery();
            
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                purchase.setPurchaseId(generatedId);
                conn.commit(); // Commit transaction
                return generatedId;
            } else {
                throw new SQLException("Creating purchase failed, no ID obtained.");
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            // Close resources
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (idStmt != null) try { idStmt.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { /* ignore */ }
            }
        }
    }
    
    @Override
    public boolean update(Purchase purchase) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setInt(1, purchase.getItemId());
            stmt.setInt(2, purchase.getQuantity());
            stmt.setInt(3, purchase.getSupplierId());
            stmt.setBigDecimal(4, purchase.getPurchaseRate());
            stmt.setBigDecimal(5, purchase.getTotalValue());
            stmt.setString(6, purchase.getPurchaseDate().toString());
            stmt.setInt(7, purchase.getPurchaseId());
            
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
    public Optional<Purchase> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPurchase(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public List<Purchase> findAll() throws SQLException {
        List<Purchase> purchases = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                purchases.add(mapResultSetToPurchase(rs));
            }
        }
        
        return purchases;
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
     * Find purchases by supplier ID
     */
    public List<Purchase> findBySupplierId(int supplierId) throws SQLException {
        String sql = "SELECT p.*, i.name as item_name, s.name as supplier_name " +
                    "FROM purchases p " +
                    "LEFT JOIN items i ON p.item_id = i.item_id " +
                    "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                    "WHERE p.supplier_id = ? ORDER BY p.purchase_date DESC";
        
        List<Purchase> purchases = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, supplierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    purchases.add(mapResultSetToPurchase(rs));
                }
            }
        }
        
        return purchases;
    }
    
    /**
     * Find purchases by date range
     */
    public List<Purchase> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT p.*, i.name as item_name, s.name as supplier_name " +
                    "FROM purchases p " +
                    "LEFT JOIN items i ON p.item_id = i.item_id " +
                    "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                    "WHERE p.purchase_date BETWEEN ? AND ? ORDER BY p.purchase_date DESC";
        
        List<Purchase> purchases = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    purchases.add(mapResultSetToPurchase(rs));
                }
            }
        }
        
        return purchases;
    }
    
    /**
     * Search purchases by supplier name or item name
     */
    public List<Purchase> searchPurchases(String searchTerm) throws SQLException {
        String sql = "SELECT p.*, i.name as item_name, s.name as supplier_name " +
                    "FROM purchases p " +
                    "LEFT JOIN items i ON p.item_id = i.item_id " +
                    "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                    "WHERE i.name LIKE ? OR s.name LIKE ? " +
                    "ORDER BY p.purchase_date DESC";
        
        List<Purchase> purchases = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    purchases.add(mapResultSetToPurchase(rs));
                }
            }
        }
        
        return purchases;
    }
    
    /**
     * Map ResultSet to Purchase object
     */
    private Purchase mapResultSetToPurchase(ResultSet rs) throws SQLException {
        Purchase purchase = new Purchase();
        
        purchase.setPurchaseId(rs.getInt("purchase_id"));
        purchase.setItemId(rs.getInt("item_id"));
        purchase.setQuantity(rs.getInt("quantity"));
        purchase.setSupplierId(rs.getInt("supplier_id"));
        purchase.setPurchaseRate(rs.getBigDecimal("purchase_rate"));
        purchase.setTotalValue(rs.getBigDecimal("total_value"));
        purchase.setPurchaseDate(LocalDate.parse(rs.getString("purchase_date")));
        
        // Parse created_date if available with defensive handling
        String createdDateStr = rs.getString("created_date");
        if (createdDateStr != null && !createdDateStr.isEmpty()) {
            try {
                // Try parsing as LocalDateTime first (ISO format with T)
                if (createdDateStr.contains("T")) {
                    purchase.setCreatedDate(LocalDateTime.parse(createdDateStr));
                } else {
                    // Handle SQLite format (space separator) - convert to ISO format
                    String isoFormat = createdDateStr.replace(" ", "T");
                    purchase.setCreatedDate(LocalDateTime.parse(isoFormat));
                }
            } catch (Exception e) {
                // Fallback: try using timestamp parsing
                try {
                    Timestamp createdTimestamp = rs.getTimestamp("created_date");
                    if (createdTimestamp != null) {
                        purchase.setCreatedDate(createdTimestamp.toLocalDateTime());
                    } else {
                        purchase.setCreatedDate(LocalDateTime.now());
                    }
                } catch (SQLException ex) {
                    // Ultimate fallback: use current time
                    purchase.setCreatedDate(LocalDateTime.now());
                }
            }
        } else {
            purchase.setCreatedDate(LocalDateTime.now());
        }
        
        // Set display properties from joins
        String itemName = rs.getString("item_name");
        if (itemName != null) {
            purchase.setItemName(itemName);
        }
        
        String supplierName = rs.getString("supplier_name");
        if (supplierName != null) {
            purchase.setSupplierName(supplierName);
        }
        
        return purchase;
    }
    
    /**
     * Get total quantity purchased for an item
     */
    public int getTotalQuantityByItemId(int itemId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM purchases WHERE item_id = ?";
        
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
     * Get average purchase rate for an item
     */
    public BigDecimal getAveragePurchaseRateByItemId(int itemId) throws SQLException {
        String sql = "SELECT AVG(purchase_rate) FROM purchases WHERE item_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal result = rs.getBigDecimal(1);
                    return result != null ? result : BigDecimal.ZERO;
                }
            }
        }
        
        return BigDecimal.ZERO;
    }
}