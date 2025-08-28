package com.erpsystem.controller;

import com.erpsystem.dao.SupplierDAO;
import com.erpsystem.model.Supplier;
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
 * Controller for the Suppliers management view
 */
public class SuppliersController implements Initializable {
    
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ObservableList<Supplier> suppliersList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // UI Controls
    @FXML private TextField searchField;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button refreshBtn;
    
    // Table and columns
    @FXML private TableView<Supplier> suppliersTable;
    @FXML private TableColumn<Supplier, Integer> supplierIdColumn;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> contactColumn;
    @FXML private TableColumn<Supplier, String> addressColumn;
    @FXML private TableColumn<Supplier, LocalDateTime> createdDateColumn;
    
    @FXML private Label recordCountLabel;
    
    // Dialog components (will be created programmatically)
    private Dialog<ButtonType> supplierDialog;
    private TextField nameField;
    private TextField contactField;
    private TextArea addressField;
    private Label validationLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupDialog();
        loadSuppliers();
        
        // Setup selection listener
        suppliersTable.getSelectionModel().selectedItemProperty().addListener(
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
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        createdDateColumn.setCellFactory(new Callback<TableColumn<Supplier, LocalDateTime>, TableCell<Supplier, LocalDateTime>>() {
            @Override
            public TableCell<Supplier, LocalDateTime> call(TableColumn<Supplier, LocalDateTime> column) {
                return new TableCell<Supplier, LocalDateTime>() {
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
        
        suppliersTable.setItems(suppliersList);
        
        // Enable row selection
        suppliersTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Double-click to edit
        suppliersTable.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editSupplier();
                }
            });
            return row;
        });
    }
    
    /**
     * Setup the add/edit dialog
     */
    private void setupDialog() {
        supplierDialog = new Dialog<>();
        supplierDialog.setTitle("Supplier Details");
        supplierDialog.setHeaderText(null);
        
        // Create form fields
        nameField = new TextField();
        nameField.setPromptText("Enter supplier name");
        
        contactField = new TextField();
        contactField.setPromptText("Enter contact information");
        
        addressField = new TextArea();
        addressField.setPromptText("Enter supplier address");
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
        
        supplierDialog.getDialogPane().setContent(grid);
        supplierDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Apply styles
        supplierDialog.getDialogPane().getStylesheets().addAll(
            MainApplication.getCurrentScene().getStylesheets()
        );
    }
    
    /**
     * Load suppliers data asynchronously
     */
    private void loadSuppliers() {
        Task<List<Supplier>> loadTask = new Task<List<Supplier>>() {
            @Override
            protected List<Supplier> call() throws Exception {
                return supplierDAO.findAll();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            List<Supplier> suppliers = loadTask.getValue();
            suppliersList.clear();
            suppliersList.addAll(suppliers);
            updateRecordCount();
        });
        
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            MainApplication.showErrorAlert("Database Error", 
                "Failed to load suppliers: " + exception.getMessage());
        });
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Update the record count label
     */
    private void updateRecordCount() {
        int count = suppliersList.size();
        recordCountLabel.setText(count + (count == 1 ? " record" : " records"));
    }
    
    @FXML
    private void searchSuppliers() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            loadSuppliers();
            return;
        }
        
        Task<List<Supplier>> searchTask = new Task<List<Supplier>>() {
            @Override
            protected List<Supplier> call() throws Exception {
                return supplierDAO.findByNameContaining(searchText);
            }
        };
        
        searchTask.setOnSucceeded(e -> {
            List<Supplier> results = searchTask.getValue();
            suppliersList.clear();
            suppliersList.addAll(results);
            updateRecordCount();
        });
        
        searchTask.setOnFailed(e -> {
            Throwable exception = searchTask.getException();
            MainApplication.showErrorAlert("Search Error", 
                "Failed to search suppliers: " + exception.getMessage());
        });
        
        Thread searchThread = new Thread(searchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }
    
    @FXML
    private void addSupplier() {
        supplierDialog.setTitle("Add New Supplier");
        
        // Clear form
        nameField.clear();
        contactField.clear();
        addressField.clear();
        validationLabel.setVisible(false);
        
        Optional<ButtonType> result = supplierDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (validateForm()) {
                Supplier supplier = new Supplier();
                supplier.setName(nameField.getText().trim());
                supplier.setContact(contactField.getText().trim());
                supplier.setAddress(addressField.getText().trim());
                
                saveSupplier(supplier, true);
            }
        }
    }
    
    @FXML
    private void editSupplier() {
        Supplier selected = suppliersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MainApplication.showWarningAlert("No Selection", 
                "Please select a supplier to edit.");
            return;
        }
        
        supplierDialog.setTitle("Edit Supplier");
        
        // Populate form with selected supplier data
        nameField.setText(selected.getName());
        contactField.setText(selected.getContact());
        addressField.setText(selected.getAddress());
        validationLabel.setVisible(false);
        
        Optional<ButtonType> result = supplierDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (validateForm()) {
                selected.setName(nameField.getText().trim());
                selected.setContact(contactField.getText().trim());
                selected.setAddress(addressField.getText().trim());
                selected.setUpdatedDate(LocalDateTime.now());
                
                saveSupplier(selected, false);
            }
        }
    }
    
    @FXML
    private void deleteSupplier() {
        Supplier selected = suppliersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MainApplication.showWarningAlert("No Selection", 
                "Please select a supplier to delete.");
            return;
        }
        
        boolean confirmed = MainApplication.showConfirmAlert("Confirm Delete", 
            "Are you sure you want to delete supplier '" + selected.getName() + "'?\n\n" +
            "This action cannot be undone.");
        
        if (confirmed) {
            Task<Void> deleteTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    supplierDAO.delete(selected.getSupplierId());
                    return null;
                }
            };
            
            deleteTask.setOnSucceeded(e -> {
                suppliersList.remove(selected);
                updateRecordCount();
                MainApplication.showInfoAlert("Success", 
                    "Supplier deleted successfully.");
            });
            
            deleteTask.setOnFailed(e -> {
                Throwable exception = deleteTask.getException();
                MainApplication.showErrorAlert("Delete Error", 
                    "Failed to delete supplier: " + exception.getMessage());
            });
            
            Thread deleteThread = new Thread(deleteTask);
            deleteThread.setDaemon(true);
            deleteThread.start();
        }
    }
    
    @FXML
    private void refreshData() {
        searchField.clear();
        loadSuppliers();
    }
    
    /**
     * Validate the form fields
     */
    private boolean validateForm() {
        String name = nameField.getText().trim();
        
        if (name.isEmpty()) {
            validationLabel.setText("Supplier name is required.");
            validationLabel.setVisible(true);
            nameField.requestFocus();
            return false;
        }
        
        if (name.length() > 100) {
            validationLabel.setText("Supplier name cannot exceed 100 characters.");
            validationLabel.setVisible(true);
            nameField.requestFocus();
            return false;
        }
        
        validationLabel.setVisible(false);
        return true;
    }
    
    /**
     * Save supplier to database
     */
    private void saveSupplier(Supplier supplier, boolean isNew) {
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (isNew) {
                    supplierDAO.insert(supplier);
                } else {
                    supplierDAO.update(supplier);
                }
                return null;
            }
        };
        
        saveTask.setOnSucceeded(e -> {
            if (isNew) {
                suppliersList.add(supplier);
                MainApplication.showInfoAlert("Success", 
                    "Supplier added successfully.");
            } else {
                suppliersTable.refresh();
                MainApplication.showInfoAlert("Success", 
                    "Supplier updated successfully.");
            }
            updateRecordCount();
        });
        
        saveTask.setOnFailed(e -> {
            Throwable exception = saveTask.getException();
            String action = isNew ? "add" : "update";
            MainApplication.showErrorAlert("Save Error", 
                "Failed to " + action + " supplier: " + exception.getMessage());
        });
        
        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }
}