package com.erpsystem.dao;

import com.erpsystem.model.Supplier;
import com.erpsystem.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Supplier entities
 */
public class SupplierDAO implements BaseDAO<Supplier, Integer> {
    
    private static final String INSERT_SQL = 
        "INSERT INTO suppliers (name, contact, address) VALUES (?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE suppliers SET name = ?, contact = ?, address = ?, updated_date = ? WHERE supplier_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM suppliers WHERE supplier_id = ?";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT * FROM suppliers WHERE supplier_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT * FROM suppliers ORDER BY name";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM suppliers";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM suppliers WHERE supplier_id = ? LIMIT 1";
    
    @Override
    public Integer insert(Supplier supplier) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement idStmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert the supplier
            stmt = conn.prepareStatement(INSERT_SQL);
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContact());
            stmt.setString(3, supplier.getAddress());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating supplier failed, no rows affected.");
            }
            
            // Get the last inserted ID using SQLite's last_insert_rowid() function
            idStmt = conn.prepareStatement("SELECT last_insert_rowid()");
            rs = idStmt.executeQuery();
            
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                supplier.setSupplierId(generatedId);
                conn.commit(); // Commit transaction
                return generatedId;
            } else {
                throw new SQLException("Creating supplier failed, no ID obtained.");
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
    public boolean update(Supplier supplier) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContact());
            stmt.setString(3, supplier.getAddress());
            // Use string format for SQLite datetime compatibility
            stmt.setString(4, LocalDateTime.now().toString());
            stmt.setInt(5, supplier.getSupplierId());
            
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
    public Optional<Supplier> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSupplier(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public List<Supplier> findAll() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        }
        
        return suppliers;
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
     * Find suppliers by name (partial match)
     */
    public List<Supplier> findByNameContaining(String name) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE name LIKE ? ORDER BY name";
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
        }
        
        return suppliers;
    }
    
    /**
     * Map ResultSet to Supplier object
     */
    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(rs.getInt("supplier_id"));
        supplier.setName(rs.getString("name"));
        supplier.setContact(rs.getString("contact"));
        supplier.setAddress(rs.getString("address"));
        
        // Handle SQLite datetime strings
        String createdDateStr = rs.getString("created_date");
        if (createdDateStr != null && !createdDateStr.isEmpty()) {
            try {
                // Try parsing as LocalDateTime first
                if (createdDateStr.contains("T")) {
                    supplier.setCreatedDate(LocalDateTime.parse(createdDateStr));
                } else {
                    // Try parsing as timestamp
                    Timestamp createdTimestamp = rs.getTimestamp("created_date");
                    if (createdTimestamp != null) {
                        supplier.setCreatedDate(createdTimestamp.toLocalDateTime());
                    }
                }
            } catch (Exception e) {
                // If parsing fails, use current time
                supplier.setCreatedDate(LocalDateTime.now());
            }
        }
        
        String updatedDateStr = rs.getString("updated_date");
        if (updatedDateStr != null && !updatedDateStr.isEmpty()) {
            try {
                // Try parsing as LocalDateTime first
                if (updatedDateStr.contains("T")) {
                    supplier.setUpdatedDate(LocalDateTime.parse(updatedDateStr));
                } else {
                    // Try parsing as timestamp
                    Timestamp updatedTimestamp = rs.getTimestamp("updated_date");
                    if (updatedTimestamp != null) {
                        supplier.setUpdatedDate(updatedTimestamp.toLocalDateTime());
                    }
                }
            } catch (Exception e) {
                // If parsing fails, use current time
                supplier.setUpdatedDate(LocalDateTime.now());
            }
        }
        
        return supplier;
    }
}