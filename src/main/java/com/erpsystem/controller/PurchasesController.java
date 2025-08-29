package com.erpsystem.controller;

import com.erpsystem.dao.PurchaseDAO;
import com.erpsystem.dao.ItemDAO;
import com.erpsystem.dao.SupplierDAO;
import com.erpsystem.model.Purchase;
import com.erpsystem.model.Item;
import com.erpsystem.model.Supplier;
import com.erpsystem.MainApplication;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Purchasing management view
 */
public class PurchasesController implements Initializable {
    
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ObservableList<Purchase> purchasesList = FXCollections.observableArrayList();
    private final ObservableList<Item> itemsList = FXCollections.observableArrayList();
    private final ObservableList<Supplier> suppliersList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // UI Controls
    @FXML private TextField searchField;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button refreshBtn;
    
    // Table and columns
    @FXML private TableView<Purchase> purchasesTable;
    @FXML private TableColumn<Purchase, Integer> purchaseIdColumn;
    @FXML private TableColumn<Purchase, LocalDate> purchaseDateColumn;
    @FXML private TableColumn<Purchase, String> itemNameColumn;
    @FXML private TableColumn<Purchase, String> supplierNameColumn;
    @FXML private TableColumn<Purchase, Integer> quantityColumn;
    @FXML private TableColumn<Purchase, BigDecimal> purchaseRateColumn;
    @FXML private TableColumn<Purchase, BigDecimal> totalValueColumn;
    
    @FXML private Label recordCountLabel;
    
    // Dialog components (will be created programmatically)
    private Dialog<ButtonType> purchaseDialog;
    private ComboBox<Item> itemComboBox;
    private ComboBox<Supplier> supplierComboBox;
    private TextField quantityField;
    private TextField purchaseRateField;
    private DatePicker purchaseDatePicker;
    private Label totalValueLabel;
    private Label validationLabel;
    
    // Current purchase being edited
    private Purchase currentPurchase;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupDialog();
        loadData();
        
        // Setup selection listener
        purchasesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                editBtn.setDisable(!hasSelection);
                deleteBtn.setDisable(!hasSelection);
            }
        );
    }
    
    /**
     * Setup the table columns and properties
     */
    private void setupTable() {
        purchaseIdColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        purchaseDateColumn.setCellFactory(new Callback<TableColumn<Purchase, LocalDate>, TableCell<Purchase, LocalDate>>() {
            @Override
            public TableCell<Purchase, LocalDate> call(TableColumn<Purchase, LocalDate> column) {
                return new TableCell<Purchase, LocalDate>() {
                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
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
        
        purchaseRateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseRate"));
        purchaseRateColumn.setCellFactory(new Callback<TableColumn<Purchase, BigDecimal>, TableCell<Purchase, BigDecimal>>() {
            @Override
            public TableCell<Purchase, BigDecimal> call(TableColumn<Purchase, BigDecimal> column) {
                return new TableCell<Purchase, BigDecimal>() {
                    @Override
                    protected void updateItem(BigDecimal item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText("$" + item.toString());
                        }
                    }
                };
            }
        });
        
        totalValueColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        totalValueColumn.setCellFactory(new Callback<TableColumn<Purchase, BigDecimal>, TableCell<Purchase, BigDecimal>>() {
            @Override
            public TableCell<Purchase, BigDecimal> call(TableColumn<Purchase, BigDecimal> column) {
                return new TableCell<Purchase, BigDecimal>() {
                    @Override
                    protected void updateItem(BigDecimal item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText("$" + item.toString());
                        }
                    }
                };
            }
        });
        
        purchasesTable.setItems(purchasesList);
        
        // Enable row selection
        purchasesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Double-click to edit
        purchasesTable.setRowFactory(tv -> {
            TableRow<Purchase> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editPurchase();
                }
            });
            return row;
        });
    }
    
    /**
     * Setup the add/edit dialog
     */
    private void setupDialog() {
        purchaseDialog = new Dialog<>();
        purchaseDialog.setTitle("Purchase Details");
        purchaseDialog.setHeaderText(null);
        
        // Create form fields
        itemComboBox = new ComboBox<>();
        itemComboBox.setPromptText("Select item");
        itemComboBox.setMaxWidth(Double.MAX_VALUE);
        itemComboBox.setItems(itemsList);
        
        // Configure item ComboBox display
        itemComboBox.setCellFactory(listView -> new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getCategory() + ")");
                }
            }
        });
        
        itemComboBox.setButtonCell(new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getCategory() + ")");
                }
            }
        });
        
        supplierComboBox = new ComboBox<>();
        supplierComboBox.setPromptText("Select supplier");
        supplierComboBox.setMaxWidth(Double.MAX_VALUE);
        supplierComboBox.setItems(suppliersList);
        
        // Configure supplier ComboBox display
        supplierComboBox.setCellFactory(listView -> new ListCell<Supplier>() {
            @Override
            protected void updateItem(Supplier supplier, boolean empty) {
                super.updateItem(supplier, empty);
                if (empty || supplier == null) {
                    setText(null);
                } else {
                    setText(supplier.getName());
                }
            }
        });
        
        supplierComboBox.setButtonCell(new ListCell<Supplier>() {
            @Override
            protected void updateItem(Supplier supplier, boolean empty) {
                super.updateItem(supplier, empty);
                if (empty || supplier == null) {
                    setText(null);
                } else {
                    setText(supplier.getName());
                }
            }
        });
        
        quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");
        
        purchaseRateField = new TextField();
        purchaseRateField.setPromptText("Enter purchase rate");
        
        purchaseDatePicker = new DatePicker();
        purchaseDatePicker.setValue(LocalDate.now());
        purchaseDatePicker.setMaxWidth(Double.MAX_VALUE);
        
        totalValueLabel = new Label("$0.00");
        totalValueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: red;");
        validationLabel.setVisible(false);
        
        // Add listeners for automatic calculation
        ChangeListener<String> calculationListener = (obs, oldVal, newVal) -> calculateTotalValue();
        quantityField.textProperty().addListener(calculationListener);
        purchaseRateField.textProperty().addListener(calculationListener);
        
        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));
        
        grid.add(new Label("Item *:"), 0, 0);
        grid.add(itemComboBox, 1, 0);
        grid.add(new Label("Supplier *:"), 0, 1);
        grid.add(supplierComboBox, 1, 1);
        grid.add(new Label("Quantity *:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Rate *:"), 0, 3);
        grid.add(purchaseRateField, 1, 3);
        grid.add(new Label("Date *:"), 0, 4);
        grid.add(purchaseDatePicker, 1, 4);
        
        // Total value display
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        totalBox.getChildren().addAll(new Label("Total Value:"), totalValueLabel);
        grid.add(totalBox, 1, 5);
        
        grid.add(validationLabel, 1, 6);
        
        purchaseDialog.getDialogPane().setContent(grid);
        purchaseDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Apply styles
        purchaseDialog.getDialogPane().getStylesheets().addAll(
            MainApplication.getCurrentScene().getStylesheets()
        );
    }
    
    /**
     * Calculate total value automatically
     */
    private void calculateTotalValue() {
        try {
            String quantityText = quantityField.getText();
            String rateText = purchaseRateField.getText();
            
            if (quantityText.isEmpty() || rateText.isEmpty()) {
                totalValueLabel.setText("$0.00");
                return;
            }
            
            int quantity = Integer.parseInt(quantityText);
            BigDecimal rate = new BigDecimal(rateText);
            BigDecimal total = rate.multiply(BigDecimal.valueOf(quantity));
            
            totalValueLabel.setText("$" + total.toString());
            
        } catch (NumberFormatException e) {
            totalValueLabel.setText("$0.00");
        }
    }
    
    /**
     * Load all data asynchronously
     */
    private void loadData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Purchase> purchases = purchaseDAO.findAll();
                List<Item> items = itemDAO.findAll();
                List<Supplier> suppliers = supplierDAO.findAll();
                
                Platform.runLater(() -> {
                    purchasesList.clear();
                    purchasesList.addAll(purchases);
                    
                    itemsList.clear();
                    itemsList.addAll(items);
                    
                    suppliersList.clear();
                    suppliersList.addAll(suppliers);
                    
                    updateRecordCount();
                });
                
                return null;
            }
        };
        
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            MainApplication.showErrorAlert("Database Error", 
                "Failed to load purchases data: " + exception.getMessage());
        });
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Update the record count label
     */
    private void updateRecordCount() {
        int count = purchasesList.size();
        recordCountLabel.setText(count + (count == 1 ? " record" : " records"));
    }
    
    @FXML
    private void addPurchase() {
        currentPurchase = null;
        clearDialogFields();
        purchaseDialog.setTitle("Add New Purchase");
        
        Optional<ButtonType> result = purchaseDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (validateInput()) {
                savePurchase();
            }
        }
    }
    
    @FXML
    private void editPurchase() {
        Purchase selected = purchasesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentPurchase = selected;
            populateDialogFields(selected);
            purchaseDialog.setTitle("Edit Purchase");
            
            Optional<ButtonType> result = purchaseDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (validateInput()) {
                    updatePurchase();
                }
            }
        }
    }
    
    @FXML
    private void deletePurchase() {
        Purchase selected = purchasesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Purchase");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this purchase?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    purchaseDAO.delete(selected.getPurchaseId());
                    purchasesList.remove(selected);
                    updateRecordCount();
                    MainApplication.showSuccessAlert("Success", "Purchase deleted successfully!");
                } catch (SQLException e) {
                    MainApplication.showErrorAlert("Database Error", 
                        "Failed to delete purchase: " + e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void refreshData() {
        loadData();
    }
    
    @FXML
    private void searchPurchases() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadData();
            return;
        }
        
        Task<List<Purchase>> searchTask = new Task<List<Purchase>>() {
            @Override
            protected List<Purchase> call() throws Exception {
                return purchaseDAO.searchPurchases(searchTerm);
            }
        };
        
        searchTask.setOnSucceeded(e -> {
            List<Purchase> results = searchTask.getValue();
            purchasesList.clear();
            purchasesList.addAll(results);
            updateRecordCount();
        });
        
        searchTask.setOnFailed(e -> {
            Throwable exception = searchTask.getException();
            MainApplication.showErrorAlert("Search Error", 
                "Failed to search purchases: " + exception.getMessage());
        });
        
        Thread searchThread = new Thread(searchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }
    
    /**
     * Clear all dialog fields
     */
    private void clearDialogFields() {
        itemComboBox.setValue(null);
        supplierComboBox.setValue(null);
        quantityField.clear();
        purchaseRateField.clear();
        purchaseDatePicker.setValue(LocalDate.now());
        totalValueLabel.setText("$0.00");
        validationLabel.setVisible(false);
    }
    
    /**
     * Populate dialog fields with purchase data
     */
    private void populateDialogFields(Purchase purchase) {
        // Find and set the item
        Item selectedItem = itemsList.stream()
            .filter(item -> item.getItemId() == purchase.getItemId())
            .findFirst().orElse(null);
        itemComboBox.setValue(selectedItem);
        
        // Find and set the supplier
        Supplier selectedSupplier = suppliersList.stream()
            .filter(supplier -> supplier.getSupplierId() == purchase.getSupplierId())
            .findFirst().orElse(null);
        supplierComboBox.setValue(selectedSupplier);
        
        quantityField.setText(String.valueOf(purchase.getQuantity()));
        purchaseRateField.setText(purchase.getPurchaseRate().toString());
        purchaseDatePicker.setValue(purchase.getPurchaseDate());
        
        calculateTotalValue();
        validationLabel.setVisible(false);
    }
    
    /**
     * Validate input fields
     */
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        if (itemComboBox.getValue() == null) {
            errors.append("Please select an item.\n");
        }
        
        if (supplierComboBox.getValue() == null) {
            errors.append("Please select a supplier.\n");
        }
        
        if (quantityField.getText().trim().isEmpty()) {
            errors.append("Please enter quantity.\n");
        } else {
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity <= 0) {
                    errors.append("Quantity must be greater than 0.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Please enter a valid quantity.\n");
            }
        }
        
        if (purchaseRateField.getText().trim().isEmpty()) {
            errors.append("Please enter purchase rate.\n");
        } else {
            try {
                BigDecimal rate = new BigDecimal(purchaseRateField.getText().trim());
                if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.append("Purchase rate must be greater than 0.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Please enter a valid purchase rate.\n");
            }
        }
        
        if (purchaseDatePicker.getValue() == null) {
            errors.append("Please select a purchase date.\n");
        }
        
        if (errors.length() > 0) {
            validationLabel.setText(errors.toString());
            validationLabel.setVisible(true);
            return false;
        }
        
        validationLabel.setVisible(false);
        return true;
    }
    
    /**
     * Save new purchase
     */
    private void savePurchase() {
        try {
            Purchase purchase = new Purchase();
            purchase.setItemId(itemComboBox.getValue().getItemId());
            purchase.setSupplierId(supplierComboBox.getValue().getSupplierId());
            purchase.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            purchase.setPurchaseRate(new BigDecimal(purchaseRateField.getText().trim()));
            purchase.setPurchaseDate(purchaseDatePicker.getValue());
            
            // Calculate total value
            BigDecimal totalValue = purchase.getPurchaseRate().multiply(BigDecimal.valueOf(purchase.getQuantity()));
            purchase.setTotalValue(totalValue);
            
            // Set display properties
            purchase.setItemName(itemComboBox.getValue().getName());
            purchase.setSupplierName(supplierComboBox.getValue().getName());
            
            // Insert into database and get the generated ID
            Integer generatedId = purchaseDAO.insert(purchase);
            
            // Set the ID on the purchase object
            purchase.setPurchaseId(generatedId);
            
            // Add to the observable list immediately for UI update
            purchasesList.add(0, purchase); // Add at the beginning for DESC order
            updateRecordCount();
            
            // Refresh table to ensure proper display
            purchasesTable.refresh();
            
            MainApplication.showSuccessAlert("Success", "Purchase added successfully!");
            
        } catch (SQLException e) {
            MainApplication.showErrorAlert("Database Error", 
                "Failed to add purchase: " + e.getMessage());
        }
    }
    
    /**
     * Update existing purchase
     */
    private void updatePurchase() {
        try {
            currentPurchase.setItemId(itemComboBox.getValue().getItemId());
            currentPurchase.setSupplierId(supplierComboBox.getValue().getSupplierId());
            currentPurchase.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            currentPurchase.setPurchaseRate(new BigDecimal(purchaseRateField.getText().trim()));
            currentPurchase.setPurchaseDate(purchaseDatePicker.getValue());
            
            // Calculate total value
            BigDecimal totalValue = currentPurchase.getPurchaseRate().multiply(BigDecimal.valueOf(currentPurchase.getQuantity()));
            currentPurchase.setTotalValue(totalValue);
            
            // Update display properties
            currentPurchase.setItemName(itemComboBox.getValue().getName());
            currentPurchase.setSupplierName(supplierComboBox.getValue().getName());
            
            purchaseDAO.update(currentPurchase);
            
            // Refresh the table
            purchasesTable.refresh();
            
            MainApplication.showSuccessAlert("Success", "Purchase updated successfully!");
            
        } catch (SQLException e) {
            MainApplication.showErrorAlert("Database Error", 
                "Failed to update purchase: " + e.getMessage());
        }
    }
}