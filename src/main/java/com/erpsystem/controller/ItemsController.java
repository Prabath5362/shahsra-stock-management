package com.erpsystem.controller;

import com.erpsystem.dao.ItemDAO;
import com.erpsystem.model.Item;
import com.erpsystem.MainApplication;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Items management view
 */
public class ItemsController implements Initializable {
    
    private final ItemDAO itemDAO = new ItemDAO();
    private final ObservableList<Item> itemsList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // UI Controls
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button refreshBtn;
    
    // Table and columns
    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, Integer> itemIdColumn;
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, String> categoryColumn;
    @FXML private TableColumn<Item, LocalDateTime> createdDateColumn;
    
    @FXML private Label recordCountLabel;
    
    // Dialog components (will be created programmatically)
    private Dialog<ButtonType> itemDialog;
    private TextField nameField;
    private ComboBox<String> categoryField;
    private Label validationLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupDialog();
        loadAllItems(); // Load all items initially
        loadCategories();
        
        // Setup selection listener
        itemsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                editBtn.setDisable(!hasSelection);
                deleteBtn.setDisable(!hasSelection);
            }
        );
        
        // Setup category filter listener
        categoryFilter.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                filterItems();
            }
        );
    }
    
    /**
     * Setup the table columns and properties
     */
    private void setupTable() {
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        createdDateColumn.setCellFactory(new Callback<TableColumn<Item, LocalDateTime>, TableCell<Item, LocalDateTime>>() {
            @Override
            public TableCell<Item, LocalDateTime> call(TableColumn<Item, LocalDateTime> column) {
                return new TableCell<Item, LocalDateTime>() {
                    @Override
                    protected void updateItem(LocalDateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText(dateFormatter.format(item));
                        }
                    }
                };
            }
        });
        
        itemsTable.setItems(itemsList);
        
        // Enable row selection
        itemsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Double-click to edit
        itemsTable.setRowFactory(tv -> {
            TableRow<Item> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editItem();
                }
            });
            return row;
        });
    }
    
    /**
     * Setup the add/edit dialog
     */
    private void setupDialog() {
        itemDialog = new Dialog<>();
        itemDialog.setTitle("Item Details");
        itemDialog.setHeaderText(null);
        
        // Create form fields
        nameField = new TextField();
        nameField.setPromptText("Enter item name");
        
        categoryField = new ComboBox<>();
        categoryField.setEditable(true);
        categoryField.setPromptText("Enter or select category");
        categoryField.setPrefWidth(200);
        
        validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: red;");
        validationLabel.setVisible(false);
        
        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));
        
        grid.add(new Label("Name *:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(validationLabel, 1, 2);
        
        itemDialog.getDialogPane().setContent(grid);
        itemDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Apply styles
        itemDialog.getDialogPane().getStylesheets().addAll(
            MainApplication.getCurrentScene().getStylesheets()
        );
    }
    
    /**
     * Load items data asynchronously
     */
    private void loadItems() {
        // Use filterItems to respect current search and category filters
        filterItems();
    }
    
    /**
     * Load all items without any filters (for initialization)
     */
    private void loadAllItems() {
        Task<List<Item>> loadTask = new Task<List<Item>>() {
            @Override
            protected List<Item> call() throws Exception {
                return itemDAO.findAll();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            List<Item> items = loadTask.getValue();
            itemsList.clear();
            itemsList.addAll(items);
            updateRecordCount();
        });
        
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            MainApplication.showErrorAlert("Database Error", 
                "Failed to load items: " + exception.getMessage());
        });
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Load categories for filter and form
     */
    private void loadCategories() {
        Task<List<String>> categoriesTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                return itemDAO.getAllCategories();
            }
        };
        
        categoriesTask.setOnSucceeded(e -> {
            List<String> categories = categoriesTask.getValue();
            
            // Update category filter
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("All Categories");
            categoryFilter.getItems().addAll(categories);
            if (categoryFilter.getSelectionModel().isEmpty()) {
                categoryFilter.getSelectionModel().selectFirst();
            }
            
            // Update dialog category field if it exists
            if (categoryField != null) {
                categoryField.getItems().clear();
                categoryField.getItems().addAll(categories);
            }
        });
        
        categoriesTask.setOnFailed(e -> {
            // If categories fail to load, still add default option
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("All Categories");
            if (categoryFilter.getSelectionModel().isEmpty()) {
                categoryFilter.getSelectionModel().selectFirst();
            }
            
            if (categoryField != null) {
                categoryField.getItems().clear();
            }
        });
        
        Thread categoriesThread = new Thread(categoriesTask);
        categoriesThread.setDaemon(true);
        categoriesThread.start();
    }
    
    /**
     * Update the record count label
     */
    private void updateRecordCount() {
        int count = itemsList.size();
        recordCountLabel.setText(count + (count == 1 ? " record" : " records"));
    }
    
    /**
     * Filter items based on search text and category
     */
    private void filterItems() {
        String searchText = searchField.getText().trim();
        String selectedCategory = categoryFilter.getSelectionModel().getSelectedItem();
        
        Task<List<Item>> filterTask = new Task<List<Item>>() {
            @Override
            protected List<Item> call() throws Exception {
                List<Item> filteredItems;
                
                // If no search text and "All Categories" selected, get all items
                if (searchText.isEmpty() && (selectedCategory == null || "All Categories".equals(selectedCategory))) {
                    filteredItems = itemDAO.findAll();
                } 
                // If only search text provided
                else if (!searchText.isEmpty() && (selectedCategory == null || "All Categories".equals(selectedCategory))) {
                    filteredItems = itemDAO.findByNameContaining(searchText);
                }
                // If only category filter provided
                else if (searchText.isEmpty() && selectedCategory != null && !"All Categories".equals(selectedCategory)) {
                    filteredItems = itemDAO.findByCategory(selectedCategory);
                }
                // Both search text and category filter provided
                else {
                    // Get items by category first, then filter by name
                    List<Item> categoryItems = itemDAO.findByCategory(selectedCategory);
                    filteredItems = categoryItems.stream()
                        .filter(item -> item.getName().toLowerCase().contains(searchText.toLowerCase()))
                        .toList();
                }
                
                return filteredItems;
            }
        };
        
        filterTask.setOnSucceeded(e -> {
            List<Item> filteredItems = filterTask.getValue();
            itemsList.clear();
            itemsList.addAll(filteredItems);
            updateRecordCount();
        });
        
        filterTask.setOnFailed(e -> {
            Throwable exception = filterTask.getException();
            MainApplication.showErrorAlert("Search Error", 
                "Failed to filter items: " + exception.getMessage());
        });
        
        Thread filterThread = new Thread(filterTask);
        filterThread.setDaemon(true);
        filterThread.start();
    }
    
    /**
     * Search for items based on search field input
     */
    @FXML
    private void searchItems() {
        filterItems();
    }
    
    @FXML
    private void addItem() {
        itemDialog.setTitle("Add New Item");
        
        // Clear form
        nameField.clear();
        categoryField.getSelectionModel().clearSelection();
        categoryField.getEditor().clear();
        validationLabel.setVisible(false);
        
        // Refresh categories in the dialog
        Task<List<String>> categoriesTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                return itemDAO.getAllCategories();
            }
        };
        
        categoriesTask.setOnSucceeded(e -> {
            List<String> categories = categoriesTask.getValue();
            categoryField.getItems().clear();
            categoryField.getItems().addAll(categories);
            
            // Show dialog after categories are loaded
            Optional<ButtonType> result = itemDialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (validateForm()) {
                    Item item = new Item();
                    item.setName(nameField.getText().trim());
                    item.setCategory(categoryField.getEditor().getText().trim());
                    
                    saveItem(item, true);
                }
            }
        });
        
        categoriesTask.setOnFailed(e -> {
            // If categories fail to load, still show dialog
            categoryField.getItems().clear();
            
            Optional<ButtonType> result = itemDialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (validateForm()) {
                    Item item = new Item();
                    item.setName(nameField.getText().trim());
                    item.setCategory(categoryField.getEditor().getText().trim());
                    
                    saveItem(item, true);
                }
            }
        });
        
        Thread categoriesThread = new Thread(categoriesTask);
        categoriesThread.setDaemon(true);
        categoriesThread.start();
    }
    
    @FXML
    private void editItem() {
        Item selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MainApplication.showWarningAlert("No Selection", 
                "Please select an item to edit.");
            return;
        }
        
        itemDialog.setTitle("Edit Item");
        
        // Refresh categories in the dialog
        Task<List<String>> categoriesTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                return itemDAO.getAllCategories();
            }
        };
        
        categoriesTask.setOnSucceeded(e -> {
            List<String> categories = categoriesTask.getValue();
            categoryField.getItems().clear();
            categoryField.getItems().addAll(categories);
            
            // Populate form with selected item data
            nameField.setText(selected.getName());
            categoryField.getEditor().setText(selected.getCategory());
            validationLabel.setVisible(false);
            
            // Show dialog after categories are loaded
            Optional<ButtonType> result = itemDialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (validateForm()) {
                    selected.setName(nameField.getText().trim());
                    selected.setCategory(categoryField.getEditor().getText().trim());
                    selected.setUpdatedDate(LocalDateTime.now());
                    
                    saveItem(selected, false);
                }
            }
        });
        
        categoriesTask.setOnFailed(e -> {
            // If categories fail to load, still show dialog
            categoryField.getItems().clear();
            
            // Populate form with selected item data
            nameField.setText(selected.getName());
            categoryField.getEditor().setText(selected.getCategory());
            validationLabel.setVisible(false);
            
            Optional<ButtonType> result = itemDialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (validateForm()) {
                    selected.setName(nameField.getText().trim());
                    selected.setCategory(categoryField.getEditor().getText().trim());
                    selected.setUpdatedDate(LocalDateTime.now());
                    
                    saveItem(selected, false);
                }
            }
        });
        
        Thread categoriesThread = new Thread(categoriesTask);
        categoriesThread.setDaemon(true);
        categoriesThread.start();
    }
    
    @FXML
    private void deleteItem() {
        Item selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MainApplication.showWarningAlert("No Selection", 
                "Please select an item to delete.");
            return;
        }
        
        boolean confirmed = MainApplication.showConfirmAlert("Confirm Delete", 
            "Are you sure you want to delete item '" + selected.getName() + "'?\n\n" +
            "This action cannot be undone.");
        
        if (confirmed) {
            Task<Void> deleteTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    itemDAO.delete(selected.getItemId());
                    return null;
                }
            };
            
            deleteTask.setOnSucceeded(e -> {
                // Remove from current list and reload all data to ensure consistency
                itemsList.remove(selected);
                updateRecordCount();
                loadItems(); // Reload all items to ensure filter consistency
                loadCategories(); // Reload categories in case category became empty
                MainApplication.showInfoAlert("Success", 
                    "Item deleted successfully.");
            });
            
            deleteTask.setOnFailed(e -> {
                Throwable exception = deleteTask.getException();
                MainApplication.showErrorAlert("Delete Error", 
                    "Failed to delete item: " + exception.getMessage());
            });
            
            Thread deleteThread = new Thread(deleteTask);
            deleteThread.setDaemon(true);
            deleteThread.start();
        }
    }
    
    @FXML
    private void refreshData() {
        searchField.clear();
        
        // Simply reload categories and then load all items
        loadCategories();
        
        // Reset category filter to "All Categories" and load all items
        Platform.runLater(() -> {
            if (categoryFilter.getItems().size() > 0) {
                categoryFilter.getSelectionModel().selectFirst(); // Select "All Categories"
            }
            loadAllItems(); // Load all items without filters
        });
    }
    
    /**
     * Validate the form fields
     */
    private boolean validateForm() {
        String name = nameField.getText().trim();
        
        if (name.isEmpty()) {
            validationLabel.setText("Item name is required.");
            validationLabel.setVisible(true);
            nameField.requestFocus();
            return false;
        }
        
        if (name.length() > 100) {
            validationLabel.setText("Item name cannot exceed 100 characters.");
            validationLabel.setVisible(true);
            nameField.requestFocus();
            return false;
        }
        
        validationLabel.setVisible(false);
        return true;
    }
    
    /**
     * Save item to database
     */
    private void saveItem(Item item, boolean isNew) {
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (isNew) {
                    itemDAO.insert(item);
                } else {
                    itemDAO.update(item);
                }
                return null;
            }
        };
        
        saveTask.setOnSucceeded(e -> {
            if (isNew) {
                MainApplication.showInfoAlert("Success", 
                    "Item added successfully.");
            } else {
                MainApplication.showInfoAlert("Success", 
                    "Item updated successfully.");
            }
            
            // Reload categories first
            loadCategories();
            
            // Then reload items based on current filter state
            String currentCategory = categoryFilter.getSelectionModel().getSelectedItem();
            if (currentCategory == null || "All Categories".equals(currentCategory)) {
                // If "All Categories" is selected, load all items
                loadAllItems();
            } else {
                // Otherwise, use filtered loading
                filterItems();
            }
        });
        
        saveTask.setOnFailed(e -> {
            Throwable exception = saveTask.getException();
            String action = isNew ? "add" : "update";
            MainApplication.showErrorAlert("Save Error", 
                "Failed to " + action + " item: " + exception.getMessage());
        });
        
        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }
}