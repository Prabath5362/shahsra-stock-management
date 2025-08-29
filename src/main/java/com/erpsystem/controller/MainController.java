package com.erpsystem.controller;

import com.erpsystem.MainApplication;
import com.erpsystem.util.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Main controller for the application layout and navigation
 */
public class MainController implements Initializable {
    
    @FXML private BorderPane mainContainer;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private VBox navigationMenu;
    
    // Header elements
    @FXML private Label headerTitle;
    @FXML private Label headerSubtitle;
    @FXML private Button themeToggleBtn;
    @FXML private Label currentUserLabel;
    
    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button suppliersBtn;
    @FXML private Button customersBtn;
    @FXML private Button itemsBtn;
    @FXML private Button purchasingBtn;
    @FXML private Button salesBtn;
    @FXML private Button inventoryBtn;
    @FXML private Button financeBtn;
    
    // Status bar elements
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    
    // Current active button for styling
    private Button activeButton;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set initial active button
        activeButton = purchasingBtn;
        updateActiveButton(purchasingBtn);
        
        // Update theme toggle button text based on current theme (now starting with dark)
        updateThemeToggleButton();
        
        // Check database connection status
        updateConnectionStatus();
        
        // Load default view (Purchasing for testing)
        showPurchasing();
        
        // Update status to reflect light theme start
        updateStatus("Welcome to ERP System - Light theme enabled");
    }
    
    @FXML
    private void toggleTheme() {
        MainApplication.toggleTheme();
        updateThemeToggleButton();
        updateStatus("Theme switched to " + (MainApplication.isDarkTheme() ? "dark" : "light") + " mode");
    }
    
    @FXML
    private void showDashboard() {
        if (loadView("/fxml/dashboard.fxml", "Dashboard")) {
            updateActiveButton(dashboardBtn);
            updateHeaderTitle("Dashboard", "Overview of your business metrics");
        }
    }
    
    @FXML
    private void showSuppliers() {
        if (loadView("/fxml/suppliers.fxml", "Suppliers Management")) {
            updateActiveButton(suppliersBtn);
            updateHeaderTitle("Suppliers", "Manage supplier master data");
        }
    }
    
    @FXML
    private void showCustomers() {
        if (loadView("/fxml/customers.fxml", "Customers Management")) {
            updateActiveButton(customersBtn);
            updateHeaderTitle("Customers", "Manage customer master data");
        }
    }
    
    @FXML
    private void showItems() {
        if (loadView("/fxml/items.fxml", "Items Management")) {
            updateActiveButton(itemsBtn);
            updateHeaderTitle("Items", "Manage item master data and pricing");
        }
    }
    
    @FXML
    private void showPurchasing() {
        if (loadView("/fxml/purchasing.fxml", "Purchasing")) {
            updateActiveButton(purchasingBtn);
            updateHeaderTitle("Purchasing", "Record and manage purchase transactions");
        }
    }
    
    @FXML
    private void showSales() {
        if (loadView("/fxml/sales.fxml", "Sales")) {
            updateActiveButton(salesBtn);
            updateHeaderTitle("Sales", "Record and manage sales transactions");
        }
    }
    
    @FXML
    private void showInventory() {
        if (loadView("/fxml/inventory.fxml", "Inventory")) {
            updateActiveButton(inventoryBtn);
            updateHeaderTitle("Inventory", "View current stock levels and valuations");
        }
    }
    
    @FXML
    private void showFinance() {
        if (loadView("/fxml/finance.fxml", "Finance")) {
            updateActiveButton(financeBtn);
            updateHeaderTitle("Finance", "Financial reports and profit analysis");
        }
    }
    
    /**
     * Load a view into the content area
     * 
     * @param fxmlPath Path to the FXML file
     * @param viewName Name of the view for error messages
     * @return true if successfully loaded, false otherwise
     */
    private boolean loadView(String fxmlPath, String viewName) {
        try {
            // Debug logging
            System.out.println("Attempting to load view: " + viewName + " from path: " + fxmlPath);
            
            // Validate resource exists
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("Resource not found: " + fxmlPath);
            }
            System.out.println("Resource found at: " + resource.toString());
            
            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();
            
            // Verify controller initialization
            Object controller = loader.getController();
            if (controller == null) {
                throw new IOException("Controller not initialized for: " + viewName);
            }
            System.out.println("Controller loaded: " + controller.getClass().getSimpleName());
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            
            updateStatus(viewName + " loaded successfully");
            System.out.println("Successfully loaded " + viewName);
            return true;
            
        } catch (IOException e) {
            System.err.println("Detailed error loading " + viewName + ": " + e.getMessage());
            e.printStackTrace();
            
            // Show a placeholder if the view doesn't exist yet
            showPlaceholder(viewName);
            updateStatus("Error loading " + viewName + " - " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error loading " + viewName + ": " + e.getMessage());
            e.printStackTrace();
            
            showPlaceholder(viewName);
            updateStatus("Unexpected error loading " + viewName + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Show a placeholder view when the actual view is not available
     */
    private void showPlaceholder(String viewName) {
        Label placeholder = new Label(viewName + " view is under development");
        placeholder.setStyle("-fx-font-size: 18; -fx-text-fill: -fx-text-secondary; -fx-padding: 50;");
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(placeholder);
    }
    
    /**
     * Update the active button styling
     */
    private void updateActiveButton(Button newActiveButton) {
        // Remove active class from previous button
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        
        // Add active class to new button
        newActiveButton.getStyleClass().add("active");
        activeButton = newActiveButton;
    }
    
    /**
     * Update the header title and subtitle
     */
    private void updateHeaderTitle(String title, String subtitle) {
        headerTitle.setText("ERP System - " + title);
        headerSubtitle.setText(subtitle);
    }
    
    /**
     * Update the theme toggle button appearance
     */
    private void updateThemeToggleButton() {
        themeToggleBtn.setText(MainApplication.isDarkTheme() ? "☀️" : "🌙");
    }
    
    /**
     * Update the status label
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * Update the database connection status
     */
    private void updateConnectionStatus() {
        try {
            boolean isConnected = DatabaseUtil.testConnection();
            if (isConnected) {
                connectionStatusLabel.setText("Database: Connected");
                connectionStatusLabel.getStyleClass().clear();
                connectionStatusLabel.getStyleClass().add("status-success");
            } else {
                connectionStatusLabel.setText("Database: Disconnected");
                connectionStatusLabel.getStyleClass().clear();
                connectionStatusLabel.getStyleClass().add("status-error");
            }
        } catch (Exception e) {
            connectionStatusLabel.setText("Database: Error");
            connectionStatusLabel.getStyleClass().clear();
            connectionStatusLabel.getStyleClass().add("status-error");
        }
    }
}