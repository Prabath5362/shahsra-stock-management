package com.erpsystem.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Database utility class for managing SQLite database connections
 * and initializing the database schema.
 */
public class DatabaseUtil {
    
    private static final String DATABASE_URL = "jdbc:sqlite:erp_system.db";
    private static boolean databaseInitialized = false;
    
    /**
     * Get a new connection to the database.
     * Creates the database and tables if they don't exist.
     * 
     * @return Connection object - caller must close this connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create new connection for each request
            Connection connection = DriverManager.getConnection(DATABASE_URL);
            
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
            // Initialize database schema only once
            if (!databaseInitialized) {
                synchronized (DatabaseUtil.class) {
                    if (!databaseInitialized) {
                        initializeDatabase(connection);
                        databaseInitialized = true;
                    }
                }
            }
            
            return connection;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }
    
    /**
     * Initialize the database by creating tables and inserting sample data.
     * 
     * @param connection The database connection to use
     * @throws SQLException if initialization fails
     */
    private static void initializeDatabase(Connection connection) throws SQLException {
        InputStream inputStream = null;
        try {
            // Try multiple ways to load the resource
            inputStream = DatabaseUtil.class.getResourceAsStream("/database_schema.sql");
            if (inputStream == null) {
                inputStream = DatabaseUtil.class.getClassLoader().getResourceAsStream("database_schema.sql");
            }
            
            if (inputStream == null) {
                throw new SQLException("Could not find database_schema.sql resource file");
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String sqlScript = reader.lines().collect(Collectors.joining("\n"));
            
            if (sqlScript == null || sqlScript.trim().isEmpty()) {
                throw new SQLException("Database schema file is empty or could not be read");
            }
            
            // Split the script into individual statements more carefully
            // First, remove comments
            StringBuilder cleanScript = new StringBuilder();
            String[] lines = sqlScript.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("--")) {
                    cleanScript.append(line).append("\n");
                }
            }
            
            // Split by semicolon but preserve multi-line statements
            String[] rawStatements = cleanScript.toString().split(";");
            
            try (Statement statement = connection.createStatement()) {
                int executedStatements = 0;
                for (String sql : rawStatements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        try {
                            System.out.println("Executing SQL: " + sql.substring(0, Math.min(sql.length(), 50)) + "...");
                            statement.execute(sql);
                            executedStatements++;
                        } catch (SQLException e) {
                            System.err.println("Error executing SQL: " + sql.substring(0, Math.min(sql.length(), 100)) + "...");
                            System.err.println("SQL Error: " + e.getMessage());
                            // Continue with other statements for non-critical errors
                            if (sql.toUpperCase().startsWith("CREATE TABLE")) {
                                // Table creation failures are critical
                                throw e;
                            }
                        }
                    }
                }
                System.out.println("Database initialized successfully! Executed " + executedStatements + " SQL statements.");
            }
            
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Failed to initialize database schema: " + e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    System.err.println("Error closing input stream: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Test the database connection.
     * 
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}