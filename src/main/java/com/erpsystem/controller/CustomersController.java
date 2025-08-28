package com.erpsystem.controller;

import com.erpsystem.dao.CustomerDAO;
import com.erpsystem.model.Customer;
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
 * Controller for the Customers management view
 */
public class CustomersController implements Initializable {
    
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ObservableList<Customer> customersList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // UI Controls
    @FXML private TextField searchField;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button refreshBtn;
    
    // Table and columns
    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, Integer> customerIdColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> contactColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private TableColumn<Customer, LocalDateTime> createdDateColumn;
    
    @FXML private Label recordCountLabel;
    
    // Dialog components (will be created programmatically)
    private Dialog<ButtonType> customerDialog;
    private TextField nameField;
    private TextField contactField;
    private TextArea addressField;
    private Label validationLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupDialog();
        loadCustomers();
        
        // Setup selection listener
        customersTable.getSelectionModel().selectedItemProperty().addListener(
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
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        createdDateColumn.setCellFactory(new Callback<TableColumn<Customer, LocalDateTime>, TableCell<Customer, LocalDateTime>>() {
            @Override
            public TableCell<Customer, LocalDateTime> call(TableColumn<Customer, LocalDateTime> column) {
                return new TableCell<Customer, LocalDateTime>() {
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
        
        customersTable.setItems(customersList);
        
        // Enable row selection
        customersTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Double-click to edit
        customersTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editCustomer();
                }
            });
            return row;
        });
    }
    
    /**
     * Setup the add/edit dialog
     */
    private void setupDialog() {
        customerDialog = new Dialog<>();
        customerDialog.setTitle("Customer Details");
        customerDialog.setHeaderText(null);
        
        // Create form fields
        nameField = new TextField();
        nameField.setPromptText("Enter customer name");
        
        contactField = new TextField();
        contactField.setPromptText("Enter contact information");
        
        addressField = new TextArea();
        addressField.setPromptText("Enter customer address");
        addressField.setPrefRowCount(3);
        
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
        grid.add(new Label("Contact:"), 0, 1);
        grid.add(contactField, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(validationLabel, 1, 3);
        
        customerDialog.getDialogPane().setContent(grid);
        customerDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Apply styles
        customerDialog.getDialogPane().getStylesheets().addAll(
            MainApplication.getCurrentScene().getStylesheets()
        );
    }
    
    /**
     * Load customers data asynchronously
     */
    private void loadCustomers() {
        Task<List<Customer>> loadTask = new Task<List<Customer>>() {
            @Override
            protected List<Customer> call() throws Exception {
                return customerDAO.findAll();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            List<Customer> customers = loadTask.getValue();
            customersList.clear();
            customersList.addAll(customers);
            updateRecordCount();
        });
        
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            MainApplication.showErrorAlert("Database Error", 
                "Failed to load customers: " + exception.getMessage());
        });
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Update the record count label
     */
    private void updateRecordCount() {
        int count = customersList.size();
        recordCountLabel.setText(count + (count == 1 ? " record" : " records"));
    }
    
    /**
     * Search for customers based on search field input
     */
    @FXML
    private void searchCustomers() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            loadCustomers();
            return;
        }
        
        Task<List<Customer>> searchTask = new Task<List<Customer>>() {
            @Override
            protected List<Customer> call() throws Exception {
                return customerDAO.findByNameContaining(searchText);
            }
        };
        
        searchTask.setOnSucceeded(e -> {
            List<Customer> results = searchTask.getValue();
            customersList.clear();
            customersList.addAll(results);
            updateRecordCount();
        });
        
        searchTask.setOnFailed(e -> {
            Throwable exception = searchTask.getException();
            MainApplication.showErrorAlert("Search Error", 
                "Failed to search customers: " + exception.getMessage());
        });
        
        Thread searchThread = new Thread(searchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }
    
    @FXML
    private void addCustomer() {
        customerDialog.setTitle("Add New Customer");
        
        // Clear form
        nameField.clear();
        contactField.clear();
        addressField.clear();
        validationLabel.setVisible(false);
        
        Optional<ButtonType> result = customerDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (validateForm()) {
                Customer customer = new Customer();
                customer.setName(nameField.getText().trim());
                customer.setContact(contactField.getText().trim());
                customer.setAddress(addressField.getText().trim());
                
                saveCustomer(customer, true);
            }
        }
    }
    
    @FXML
    private void editCustomer() {
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MainApplication.showWarningAlert("No Selection", 
                "Please select a customer to edit.");
            return;
        }
        
        customerDialog.setTitle("Edit Customer");
        
        // Populate form with selected customer data
        nameField.setText(selected.getName());
        contactField.setText(selected.getContact());
        addressField.setText(selected.getAddress());
        validationLabel.setVisible(false);
        
        Optional<ButtonType> result = customerDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (validateForm()) {
                selected.setName(nameField.getText().trim());
                selected.setContact(contactField.getText().trim());
                selected.setAddress(addressField.getText().trim());
                selected.setUpdatedDate(LocalDateTime.now());
                
                saveCustomer(selected, false);
            }
        }
    }
    
    @FXML
    private void deleteCustomer() {
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MainApplication.showWarningAlert("No Selection", 
                "Please select a customer to delete.");
            return;
        }
        
        boolean confirmed = MainApplication.showConfirmAlert("Confirm Delete", 
            "Are you sure you want to delete customer '" + selected.getName() + "'?\n\n" +
            "This action cannot be undone.");
        
        if (confirmed) {
            Task<Void> deleteTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    customerDAO.delete(selected.getCustomerId());
                    return null;
                }
            };
            
            deleteTask.setOnSucceeded(e -> {
                customersList.remove(selected);
                updateRecordCount();
                MainApplication.showInfoAlert("Success", 
                    "Customer deleted successfully.");
            });
            
            deleteTask.setOnFailed(e -> {
                Throwable exception = deleteTask.getException();
                MainApplication.showErrorAlert("Delete Error", 
                    "Failed to delete customer: " + exception.getMessage());
            });
            
            Thread deleteThread = new Thread(deleteTask);
            deleteThread.setDaemon(true);
            deleteThread.start();
        }
    }
    
    @FXML
    private void refreshData() {
        searchField.clear();
        loadCustomers();
    }
    
    /**
     * Validate the form fields
     */
    private boolean validateForm() {
        String name = nameField.getText().trim();
        
        if (name.isEmpty()) {
            validationLabel.setText("Customer name is required.");
            validationLabel.setVisible(true);
            nameField.requestFocus();
            return false;
        }
        
        if (name.length() > 100) {
            validationLabel.setText("Customer name cannot exceed 100 characters.");
            validationLabel.setVisible(true);
            nameField.requestFocus();
            return false;
        }
        
        validationLabel.setVisible(false);
        return true;
    }
    
    /**
     * Save customer to database
     */
    private void saveCustomer(Customer customer, boolean isNew) {
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (isNew) {
                    customerDAO.insert(customer);
                } else {
                    customerDAO.update(customer);
                }
                return null;
            }
        };
        
        saveTask.setOnSucceeded(e -> {
            if (isNew) {
                customersList.add(customer);
                MainApplication.showInfoAlert("Success", 
                    "Customer added successfully.");
            } else {
                customersTable.refresh();
                MainApplication.showInfoAlert("Success", 
                    "Customer updated successfully.");
            }
            updateRecordCount();
        });
        
        saveTask.setOnFailed(e -> {
            Throwable exception = saveTask.getException();
            String action = isNew ? "add" : "update";
            MainApplication.showErrorAlert("Save Error", 
                "Failed to " + action + " customer: " + exception.getMessage());
        });
        
        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }
}