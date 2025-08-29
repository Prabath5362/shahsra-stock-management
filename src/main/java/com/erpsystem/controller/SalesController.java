package com.erpsystem.controller;

import com.erpsystem.MainApplication;
import com.erpsystem.dao.CustomerDAO;
import com.erpsystem.dao.ItemDAO;
import com.erpsystem.dao.SaleDAO;
import com.erpsystem.model.Customer;
import com.erpsystem.model.Item;
import com.erpsystem.model.Sale;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
 * Controller for sales management functionality
 */
public class SalesController implements Initializable {
    
    private final SaleDAO saleDAO = new SaleDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ObservableList<Sale> salesList = FXCollections.observableArrayList();
    private final ObservableList<Item> itemsList = FXCollections.observableArrayList();
    private final ObservableList<Customer> customersList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // UI Controls
    @FXML private TextField searchField;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button refreshBtn;
    
    // Table and columns
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> saleIdColumn;
    @FXML private TableColumn<Sale, LocalDate> saleDateColumn;
    @FXML private TableColumn<Sale, String> itemNameColumn;
    @FXML private TableColumn<Sale, String> customerNameColumn;
    @FXML private TableColumn<Sale, Integer> quantityColumn;
    @FXML private TableColumn<Sale, BigDecimal> salesRateColumn;
    @FXML private TableColumn<Sale, BigDecimal> totalValueColumn;
    
    @FXML private Label recordCountLabel;
    
    // Dialog components (will be created programmatically)
    private Dialog<ButtonType> saleDialog;
    private ComboBox<Item> itemComboBox;
    private ComboBox<Customer> customerComboBox;
    private TextField quantityField;
    private TextField salesRateField;
    private DatePicker saleDatePicker;
    private Label totalValueLabel;
    private Label validationLabel;
    
    // Current sale being edited
    private Sale currentSale;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupDialog();
        loadData();
        
        // Setup selection listener
        salesTable.getSelectionModel().selectedItemProperty().addListener(
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
        saleIdColumn.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        saleDateColumn.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        saleDateColumn.setCellFactory(new Callback<TableColumn<Sale, LocalDate>, TableCell<Sale, LocalDate>>() {
            @Override
            public TableCell<Sale, LocalDate> call(TableColumn<Sale, LocalDate> column) {
                return new TableCell<Sale, LocalDate>() {
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
        
        salesRateColumn.setCellValueFactory(new PropertyValueFactory<>("salesRate"));
        salesRateColumn.setCellFactory(new Callback<TableColumn<Sale, BigDecimal>, TableCell<Sale, BigDecimal>>() {
            @Override
            public TableCell<Sale, BigDecimal> call(TableColumn<Sale, BigDecimal> column) {
                return new TableCell<Sale, BigDecimal>() {
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
        totalValueColumn.setCellFactory(new Callback<TableColumn<Sale, BigDecimal>, TableCell<Sale, BigDecimal>>() {
            @Override
            public TableCell<Sale, BigDecimal> call(TableColumn<Sale, BigDecimal> column) {
                return new TableCell<Sale, BigDecimal>() {
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
        
        salesTable.setItems(salesList);
        
        // Enable row selection
        salesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Double-click to edit
        salesTable.setRowFactory(tv -> {
            TableRow<Sale> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editSale();
                }
            });
            return row;
        });
    }
    
    /**
     * Setup the add/edit dialog
     */
    private void setupDialog() {
        saleDialog = new Dialog<>();
        saleDialog.setTitle("Sale Details");
        saleDialog.setHeaderText(null);
        
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
        
        customerComboBox = new ComboBox<>();
        customerComboBox.setPromptText("Select customer");
        customerComboBox.setMaxWidth(Double.MAX_VALUE);
        customerComboBox.setItems(customersList);
        
        // Configure customer ComboBox display
        customerComboBox.setCellFactory(listView -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                if (empty || customer == null) {
                    setText(null);
                } else {
                    setText(customer.getName());
                }
            }
        });
        
        customerComboBox.setButtonCell(new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                if (empty || customer == null) {
                    setText(null);
                } else {
                    setText(customer.getName());
                }
            }
        });
        
        quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");
        
        salesRateField = new TextField();
        salesRateField.setPromptText("Enter sales rate");
        
        saleDatePicker = new DatePicker();
        saleDatePicker.setValue(LocalDate.now());
        saleDatePicker.setMaxWidth(Double.MAX_VALUE);
        
        totalValueLabel = new Label("$0.00");
        totalValueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: red;");
        validationLabel.setVisible(false);
        
        // Add listeners for automatic calculation
        ChangeListener<String> calculationListener = (obs, oldVal, newVal) -> calculateTotalValue();
        quantityField.textProperty().addListener(calculationListener);
        salesRateField.textProperty().addListener(calculationListener);
        
        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        grid.add(new Label("Item *:"), 0, 0);
        grid.add(itemComboBox, 1, 0);
        grid.add(new Label("Customer *:"), 0, 1);
        grid.add(customerComboBox, 1, 1);
        grid.add(new Label("Quantity *:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Rate *:"), 0, 3);
        grid.add(salesRateField, 1, 3);
        grid.add(new Label("Date *:"), 0, 4);
        grid.add(saleDatePicker, 1, 4);
        
        // Total value display
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        totalBox.getChildren().addAll(new Label("Total Value:"), totalValueLabel);
        grid.add(totalBox, 1, 5);
        
        grid.add(validationLabel, 1, 6);
        
        saleDialog.getDialogPane().setContent(grid);
        saleDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Apply styles
        saleDialog.getDialogPane().getStylesheets().addAll(
            MainApplication.getCurrentScene().getStylesheets()
        );
    }
    
    /**
     * Calculate total value automatically
     */
    private void calculateTotalValue() {
        try {
            String quantityText = quantityField.getText();
            String rateText = salesRateField.getText();
            
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
                List<Sale> sales = saleDAO.findAll();
                List<Item> items = itemDAO.findAll();
                List<Customer> customers = customerDAO.findAll();
                
                Platform.runLater(() -> {
                    salesList.clear();
                    salesList.addAll(sales);
                    
                    itemsList.clear();
                    itemsList.addAll(items);
                    
                    customersList.clear();
                    customersList.addAll(customers);
                    
                    updateRecordCount();
                });
                
                return null;
            }
        };
        
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            MainApplication.showErrorAlert("Database Error", 
                "Failed to load sales data: " + exception.getMessage());
        });
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Update the record count label
     */
    private void updateRecordCount() {
        int count = salesList.size();
        recordCountLabel.setText(count + (count == 1 ? " record" : " records"));
    }
    
    @FXML
    private void addSale() {
        currentSale = null;
        clearDialogFields();
        saleDialog.setTitle("Add New Sale");
        
        Optional<ButtonType> result = saleDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (validateInput()) {
                saveSale();
            }
        }
    }
    
    @FXML
    private void editSale() {
        Sale selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentSale = selected;
            populateDialogFields(selected);
            saleDialog.setTitle("Edit Sale");
            
            Optional<ButtonType> result = saleDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (validateInput()) {
                    updateSale();
                }
            }
        }
    }
    
    @FXML
    private void deleteSale() {
        Sale selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Sale");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this sale?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    saleDAO.delete(selected.getSaleId());
                    salesList.remove(selected);
                    updateRecordCount();
                    MainApplication.showSuccessAlert("Success", "Sale deleted successfully!");
                } catch (SQLException e) {
                    MainApplication.showErrorAlert("Database Error", 
                        "Failed to delete sale: " + e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void refreshData() {
        loadData();
    }
    
    @FXML
    private void searchSales() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadData();
            return;
        }
        
        Task<List<Sale>> searchTask = new Task<List<Sale>>() {
            @Override
            protected List<Sale> call() throws Exception {
                return saleDAO.searchSales(searchTerm);
            }
        };
        
        searchTask.setOnSucceeded(e -> {
            List<Sale> results = searchTask.getValue();
            salesList.clear();
            salesList.addAll(results);
            updateRecordCount();
        });
        
        searchTask.setOnFailed(e -> {
            Throwable exception = searchTask.getException();
            MainApplication.showErrorAlert("Search Error", 
                "Failed to search sales: " + exception.getMessage());
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
        customerComboBox.setValue(null);
        quantityField.clear();
        salesRateField.clear();
        saleDatePicker.setValue(LocalDate.now());
        totalValueLabel.setText("$0.00");
        validationLabel.setVisible(false);
    }
    
    /**
     * Populate dialog fields with sale data
     */
    private void populateDialogFields(Sale sale) {
        // Find and set the item
        Item selectedItem = itemsList.stream()
            .filter(item -> item.getItemId() == sale.getItemId())
            .findFirst().orElse(null);
        itemComboBox.setValue(selectedItem);
        
        // Find and set the customer
        Customer selectedCustomer = customersList.stream()
            .filter(customer -> customer.getCustomerId() == sale.getCustomerId())
            .findFirst().orElse(null);
        customerComboBox.setValue(selectedCustomer);
        
        quantityField.setText(String.valueOf(sale.getQuantity()));
        salesRateField.setText(sale.getSalesRate().toString());
        saleDatePicker.setValue(sale.getSaleDate());
        
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
        
        if (customerComboBox.getValue() == null) {
            errors.append("Please select a customer.\n");
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
        
        if (salesRateField.getText().trim().isEmpty()) {
            errors.append("Please enter sales rate.\n");
        } else {
            try {
                BigDecimal rate = new BigDecimal(salesRateField.getText().trim());
                if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.append("Sales rate must be greater than 0.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Please enter a valid sales rate.\n");
            }
        }
        
        if (saleDatePicker.getValue() == null) {
            errors.append("Please select a sale date.\n");
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
     * Save new sale
     */
    private void saveSale() {
        try {
            Sale sale = new Sale();
            sale.setItemId(itemComboBox.getValue().getItemId());
            sale.setCustomerId(customerComboBox.getValue().getCustomerId());
            sale.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            sale.setSalesRate(new BigDecimal(salesRateField.getText().trim()));
            sale.setSaleDate(saleDatePicker.getValue());
            
            // Calculate total value
            BigDecimal totalValue = sale.getSalesRate().multiply(BigDecimal.valueOf(sale.getQuantity()));
            sale.setTotalValue(totalValue);
            
            // Set display properties
            sale.setItemName(itemComboBox.getValue().getName());
            sale.setCustomerName(customerComboBox.getValue().getName());
            
            // Insert into database and get the generated ID
            Integer generatedId = saleDAO.insert(sale);
            
            // Set the ID on the sale object
            sale.setSaleId(generatedId);
            
            // Add to the observable list immediately for UI update
            salesList.add(0, sale); // Add at the beginning for DESC order
            updateRecordCount();
            
            // Refresh table to ensure proper display
            salesTable.refresh();
            
            MainApplication.showSuccessAlert("Success", "Sale added successfully!");
            
        } catch (SQLException e) {
            MainApplication.showErrorAlert("Database Error", 
                "Failed to add sale: " + e.getMessage());
        }
    }
    
    /**
     * Update existing sale
     */
    private void updateSale() {
        try {
            currentSale.setItemId(itemComboBox.getValue().getItemId());
            currentSale.setCustomerId(customerComboBox.getValue().getCustomerId());
            currentSale.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            currentSale.setSalesRate(new BigDecimal(salesRateField.getText().trim()));
            currentSale.setSaleDate(saleDatePicker.getValue());
            
            // Calculate total value
            BigDecimal totalValue = currentSale.getSalesRate().multiply(BigDecimal.valueOf(currentSale.getQuantity()));
            currentSale.setTotalValue(totalValue);
            
            // Update display properties
            currentSale.setItemName(itemComboBox.getValue().getName());
            currentSale.setCustomerName(customerComboBox.getValue().getName());
            
            saleDAO.update(currentSale);
            
            // Refresh the table
            salesTable.refresh();
            
            MainApplication.showSuccessAlert("Success", "Sale updated successfully!");
            
        } catch (SQLException e) {
            MainApplication.showErrorAlert("Database Error", 
                "Failed to update sale: " + e.getMessage());
        }
    }
}