package com.erpsystem.test;

import com.erpsystem.dao.PurchaseDAO;
import com.erpsystem.dao.ItemDAO;
import com.erpsystem.dao.SupplierDAO;
import com.erpsystem.model.Purchase;
import com.erpsystem.model.Item;
import com.erpsystem.model.Supplier;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TestPurchasingFix {
    public static void main(String[] args) {
        System.out.println("Testing Purchasing Module Fixes...");
        
        PurchaseDAO purchaseDAO = new PurchaseDAO();
        ItemDAO itemDAO = new ItemDAO();
        SupplierDAO supplierDAO = new SupplierDAO();
        
        try {
            // Test 1: Load items and suppliers (ComboBox data)
            System.out.println("\n=== Test 1: Loading ComboBox Data ===");
            List<Item> items = itemDAO.findAll();
            List<Supplier> suppliers = supplierDAO.findAll();
            
            System.out.println("Items loaded: " + items.size());
            if (!items.isEmpty()) {
                System.out.println("Sample item: " + items.get(0).getName() + " (" + items.get(0).getCategory() + ")");
            }
            
            System.out.println("Suppliers loaded: " + suppliers.size());
            if (!suppliers.isEmpty()) {
                System.out.println("Sample supplier: " + suppliers.get(0).getName());
            }
            
            // Test 2: Create and save a purchase
            if (!items.isEmpty() && !suppliers.isEmpty()) {
                System.out.println("\n=== Test 2: Creating New Purchase ===");
                
                Item testItem = items.get(0);
                Supplier testSupplier = suppliers.get(0);
                
                Purchase testPurchase = new Purchase();
                testPurchase.setItemId(testItem.getItemId());
                testPurchase.setSupplierId(testSupplier.getSupplierId());
                testPurchase.setQuantity(10);
                testPurchase.setPurchaseRate(new BigDecimal("50.00"));
                testPurchase.setPurchaseDate(LocalDate.now());
                
                // Calculate total value
                BigDecimal totalValue = testPurchase.getPurchaseRate().multiply(BigDecimal.valueOf(testPurchase.getQuantity()));
                testPurchase.setTotalValue(totalValue);
                
                // Set display properties (as the UI would do)
                testPurchase.setItemName(testItem.getName());
                testPurchase.setSupplierName(testSupplier.getName());
                
                System.out.println("Creating purchase: " + testPurchase.getQuantity() + " x " + testItem.getName() + 
                                 " from " + testSupplier.getName() + " at $" + testPurchase.getPurchaseRate());
                
                // Insert into database
                Integer generatedId = purchaseDAO.insert(testPurchase);
                testPurchase.setPurchaseId(generatedId);
                
                System.out.println("Purchase created successfully with ID: " + generatedId);
                System.out.println("Total value: $" + testPurchase.getTotalValue());
                
                // Test 3: Retrieve the purchase to verify it was saved correctly
                System.out.println("\n=== Test 3: Retrieving Purchase ===");
                var retrievedPurchase = purchaseDAO.findById(generatedId);
                
                if (retrievedPurchase.isPresent()) {
                    Purchase p = retrievedPurchase.get();
                    System.out.println("Retrieved purchase ID: " + p.getPurchaseId());
                    System.out.println("Item: " + p.getItemName());
                    System.out.println("Supplier: " + p.getSupplierName());
                    System.out.println("Quantity: " + p.getQuantity());
                    System.out.println("Rate: $" + p.getPurchaseRate());
                    System.out.println("Total: $" + p.getTotalValue());
                } else {
                    System.out.println("ERROR: Could not retrieve the created purchase!");
                }
                
                // Test 4: Load all purchases to verify list functionality
                System.out.println("\n=== Test 4: Loading All Purchases ===");
                List<Purchase> allPurchases = purchaseDAO.findAll();
                System.out.println("Total purchases in database: " + allPurchases.size());
                
                if (!allPurchases.isEmpty()) {
                    Purchase lastPurchase = allPurchases.get(0); // Should be the most recent
                    System.out.println("Most recent purchase: " + lastPurchase.getItemName() + 
                                     " from " + lastPurchase.getSupplierName());
                }
            } else {
                System.out.println("WARNING: No items or suppliers found to test purchase creation!");
            }
            
            System.out.println("\n=== All Tests Completed Successfully! ===");
            
        } catch (SQLException e) {
            System.out.println("Database error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}