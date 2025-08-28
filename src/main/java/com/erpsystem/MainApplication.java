package com.erpsystem;

import com.erpsystem.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX Application class for the ERP System
 */
public class MainApplication extends Application {
    
    private static Stage primaryStage;
    private static Scene currentScene;
    private static boolean isDarkTheme = false;
    
    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        // Test database connection
        if (!DatabaseUtil.testConnection()) {
            showErrorAlert("Database Connection Failed", 
                "Unable to connect to the database. Please check your configuration.");
            return;
        }
        
        // Load the main layout
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/fxml/main-layout.fxml"));
        currentScene = new Scene(fxmlLoader.load(), 1400, 900);
        
        // Apply light theme as default
        applyTheme(false);
        
        primaryStage.setTitle("ERP System - Enterprise Resource Planning");
        primaryStage.setScene(currentScene);
        
        // Set minimum size and make resizable
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);
        primaryStage.setResizable(true);
        
        // Maximize window but keep window controls visible
        primaryStage.setMaximized(true);
        
        // Ensure window stays on top briefly and then normal
        primaryStage.setAlwaysOnTop(false);
        
        // Handle application close
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        
        primaryStage.show();
        
        // Ensure window is properly maximized with controls visible
        primaryStage.toFront();
    }
    
    /**
     * Apply theme to the current scene
     * 
     * @param darkTheme true for dark theme, false for light theme
     */
    public static void applyTheme(boolean darkTheme) {
        if (currentScene == null) return;
        
        isDarkTheme = darkTheme;
        
        // Clear existing stylesheets
        currentScene.getStylesheets().clear();
        
        // Add the appropriate theme
        String themeFile = darkTheme ? "/css/theme-dark.css" : "/css/theme-light.css";
        currentScene.getStylesheets().add(MainApplication.class.getResource(themeFile).toExternalForm());
    }
    
    /**
     * Toggle between dark and light theme
     */
    public static void toggleTheme() {
        applyTheme(!isDarkTheme);
    }
    
    /**
     * Get the current theme state
     * 
     * @return true if dark theme is active, false otherwise
     */
    public static boolean isDarkTheme() {
        return isDarkTheme;
    }
    
    /**
     * Get the primary stage
     * 
     * @return the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Get the current scene
     * 
     * @return the current scene
     */
    public static Scene getCurrentScene() {
        return currentScene;
    }
    
    /**
     * Show an error alert dialog
     * 
     * @param title The title of the alert
     * @param message The message to display
     */
    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply current theme to dialog
        applyThemeToDialog(alert);
        
        alert.showAndWait();
    }
    
    /**
     * Show an information alert dialog
     * 
     * @param title The title of the alert
     * @param message The message to display
     */
    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply current theme to dialog
        applyThemeToDialog(alert);
        
        alert.showAndWait();
    }
    
    /**
     * Show a warning alert dialog
     * 
     * @param title The title of the alert
     * @param message The message to display
     */
    public static void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply current theme to dialog
        applyThemeToDialog(alert);
        
        alert.showAndWait();
    }
    
    /**
     * Show a confirmation alert dialog
     * 
     * @param title The title of the alert
     * @param message The message to display
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply current theme to dialog
        applyThemeToDialog(alert);
        
        return alert.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK;
    }
    
    /**
     * Apply current theme to a dialog
     * 
     * @param alert The alert dialog to style
     */
    private static void applyThemeToDialog(Alert alert) {
        if (currentScene != null && !currentScene.getStylesheets().isEmpty()) {
            alert.getDialogPane().getStylesheets().addAll(currentScene.getStylesheets());
        }
    }
    
    public static void main(String[] args) {
        launch();
    }
}