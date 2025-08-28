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
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, purchase.getItemId());
            stmt.setInt(2, purchase.getQuantity());
            stmt.setInt(3, purchase.getSupplierId());
            stmt.setBigDecimal(4, purchase.getPurchaseRate());
            stmt.setBigDecimal(5, purchase.getTotalValue());
            stmt.setDate(6, Date.valueOf(purchase.getPurchaseDate()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating purchase failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    purchase.setPurchaseId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating purchase failed, no ID obtained.");
                }
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
            stmt.setDate(6, Date.valueOf(purchase.getPurchaseDate()));
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
     * Find purchases by item ID
     */
    public List<Purchase> findByItemId(int itemId) throws SQLException {
        String sql = "SELECT p.*, i.name as item_name, s.name as supplier_name " +
                    "FROM purchases p " +
                    "LEFT JOIN items i ON p.item_id = i.item_id " +
                    "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                    "WHERE p.item_id = ? ORDER BY p.purchase_date DESC";
        
        List<Purchase> purchases = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    purchases.add(mapResultSetToPurchase(rs));
                }
            }
        }
        
        return purchases;
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
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    purchases.add(mapResultSetToPurchase(rs));
                }
            }
        }
        
        return purchases;
    }
    
    /**
     * Get total purchase quantity for an item
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
     * Get average purchase rate for an item (weighted by quantity)
     */
    public BigDecimal getAveragePurchaseRateByItemId(int itemId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(purchase_rate * quantity) / SUM(quantity), 0) " +
                    "FROM purchases WHERE item_id = ? AND quantity > 0";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
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
        purchase.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
        
        Timestamp createdTimestamp = rs.getTimestamp("created_date");
        if (createdTimestamp != null) {
            purchase.setCreatedDate(createdTimestamp.toLocalDateTime());
        }
        
        // Set display names if available
        purchase.setItemName(rs.getString("item_name"));
        purchase.setSupplierName(rs.getString("supplier_name"));
        
        return purchase;
    }
}