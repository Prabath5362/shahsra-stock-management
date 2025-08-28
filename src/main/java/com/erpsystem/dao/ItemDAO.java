package com.erpsystem.dao;

import com.erpsystem.model.Item;
import com.erpsystem.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Item entities
 */
public class ItemDAO implements BaseDAO<Item, Integer> {
    
    private static final String INSERT_SQL = 
        "INSERT INTO items (name, category) VALUES (?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE items SET name = ?, category = ?, updated_date = ? WHERE item_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM items WHERE item_id = ?";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT * FROM items WHERE item_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT * FROM items ORDER BY name";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM items";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM items WHERE item_id = ? LIMIT 1";
    
    @Override
    public Integer insert(Item item) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement idStmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert the item
            stmt = conn.prepareStatement(INSERT_SQL);
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getCategory());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating item failed, no rows affected.");
            }
            
            // Get the last inserted ID using SQLite's last_insert_rowid() function
            idStmt = conn.prepareStatement("SELECT last_insert_rowid()");
            rs = idStmt.executeQuery();
            
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                item.setItemId(generatedId);
                conn.commit(); // Commit transaction
                return generatedId;
            } else {
                throw new SQLException("Creating item failed, no ID obtained.");
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
            // Close resources in reverse order
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (idStmt != null) try { idStmt.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) { /* ignore */ }
            }
        }
    }
    
    @Override
    public boolean update(Item item) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getCategory());
            // Use string format for SQLite datetime compatibility
            stmt.setString(3, LocalDateTime.now().toString());
            stmt.setInt(4, item.getItemId());
            
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
    public Optional<Item> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToItem(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public List<Item> findAll() throws SQLException {
        List<Item> items = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        
        return items;
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
     * Find items by name (partial match)
     */
    public List<Item> findByNameContaining(String name) throws SQLException {
        String sql = "SELECT * FROM items WHERE name LIKE ? ORDER BY name";
        List<Item> items = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        }
        
        return items;
    }
    
    /**
     * Find items by category
     */
    public List<Item> findByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM items WHERE category = ? ORDER BY name";
        List<Item> items = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        }
        
        return items;
    }
    
    /**
     * Get all distinct categories
     */
    public List<String> getAllCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM items WHERE category IS NOT NULL ORDER BY category";
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String category = rs.getString("category");
                if (category != null && !category.trim().isEmpty()) {
                    categories.add(category);
                }
            }
        }
        
        return categories;
    }
    
    /**
     * Map ResultSet to Item object
     */
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setName(rs.getString("name"));
        item.setCategory(rs.getString("category"));
        
        // Handle datetime parsing with fallback for SQLite compatibility
        String createdDateStr = rs.getString("created_date");
        if (createdDateStr != null) {
            try {
                item.setCreatedDate(LocalDateTime.parse(createdDateStr));
            } catch (Exception e) {
                // Fallback to timestamp parsing if string parsing fails
                Timestamp createdTimestamp = rs.getTimestamp("created_date");
                if (createdTimestamp != null) {
                    item.setCreatedDate(createdTimestamp.toLocalDateTime());
                }
            }
        }
        
        String updatedDateStr = rs.getString("updated_date");
        if (updatedDateStr != null) {
            try {
                item.setUpdatedDate(LocalDateTime.parse(updatedDateStr));
            } catch (Exception e) {
                // Fallback to timestamp parsing if string parsing fails
                Timestamp updatedTimestamp = rs.getTimestamp("updated_date");
                if (updatedTimestamp != null) {
                    item.setUpdatedDate(updatedTimestamp.toLocalDateTime());
                }
            }
        }
        
        return item;
    }
}