package com.erpsystem.service;

import com.erpsystem.dao.ItemDAO;
import com.erpsystem.dao.PurchaseDAO;
import com.erpsystem.dao.SaleDAO;
import com.erpsystem.model.InventoryItem;
import com.erpsystem.model.Item;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for inventory calculations and operations
 */
public class InventoryService {
    
    private final ItemDAO itemDAO;
    private final PurchaseDAO purchaseDAO;
    private final SaleDAO saleDAO;
    
    public InventoryService() {
        this.itemDAO = new ItemDAO();
        this.purchaseDAO = new PurchaseDAO();
        this.saleDAO = new SaleDAO();
    }
    
    /**
     * Calculate inventory for all items
     * 
     * @return List of InventoryItem with calculated balances and values
     * @throws SQLException if database operation fails
     */
    public List<InventoryItem> calculateInventory() throws SQLException {
        List<InventoryItem> inventoryItems = new ArrayList<>();
        List<Item> items = itemDAO.findAll();
        
        for (Item item : items) {
            InventoryItem inventoryItem = calculateInventoryForItem(item);
            inventoryItems.add(inventoryItem);
        }
        
        return inventoryItems;
    }
    
    /**
     * Calculate inventory for a specific item
     * 
     * @param item The item to calculate inventory for
     * @return InventoryItem with calculated balance and values
     * @throws SQLException if database operation fails
     */
    public InventoryItem calculateInventoryForItem(Item item) throws SQLException {
        int totalPurchased = purchaseDAO.getTotalQuantityByItemId(item.getItemId());
        int totalSold = saleDAO.getTotalQuantityByItemId(item.getItemId());
        int balanceQuantity = totalPurchased - totalSold;
        
        // Get average rates from actual transactions
        BigDecimal avgPurchaseRate = purchaseDAO.getAveragePurchaseRateByItemId(item.getItemId());
        BigDecimal avgSalesRate = saleDAO.getAverageSalesRateByItemId(item.getItemId());
        
        return new InventoryItem(
            item.getItemId(),
            item.getName(),
            item.getCategory(),
            balanceQuantity,
            avgPurchaseRate,
            avgSalesRate
        );
    }
    
    /**
     * Get inventory items with low stock (quantity <= threshold)
     * 
     * @param threshold The low stock threshold
     * @return List of InventoryItem with low stock
     * @throws SQLException if database operation fails
     */
    public List<InventoryItem> getLowStockItems(int threshold) throws SQLException {
        List<InventoryItem> inventoryItems = calculateInventory();
        return inventoryItems.stream()
            .filter(item -> item.getBalanceQuantity() <= threshold)
            .toList();
    }
    
    /**
     * Get inventory items with no stock
     * 
     * @return List of InventoryItem with zero or negative stock
     * @throws SQLException if database operation fails
     */
    public List<InventoryItem> getOutOfStockItems() throws SQLException {
        return getLowStockItems(0);
    }
    
    /**
     * Calculate total inventory value at purchase rates
     * 
     * @return Total purchase value of all inventory
     * @throws SQLException if database operation fails
     */
    public BigDecimal getTotalInventoryPurchaseValue() throws SQLException {
        List<InventoryItem> inventoryItems = calculateInventory();
        return inventoryItems.stream()
            .map(InventoryItem::getPurchaseValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate total inventory value at sales rates
     * 
     * @return Total sales value of all inventory
     * @throws SQLException if database operation fails
     */
    public BigDecimal getTotalInventorySalesValue() throws SQLException {
        List<InventoryItem> inventoryItems = calculateInventory();
        return inventoryItems.stream()
            .map(InventoryItem::getSalesValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate potential profit from current inventory
     * 
     * @return Potential profit (sales value - purchase value)
     * @throws SQLException if database operation fails
     */
    public BigDecimal getPotentialProfit() throws SQLException {
        BigDecimal salesValue = getTotalInventorySalesValue();
        BigDecimal purchaseValue = getTotalInventoryPurchaseValue();
        return salesValue.subtract(purchaseValue);
    }
    
    /**
     * Get inventory summary statistics
     * 
     * @return InventorySummary object with key metrics
     * @throws SQLException if database operation fails
     */
    public InventorySummary getInventorySummary() throws SQLException {
        List<InventoryItem> inventoryItems = calculateInventory();
        
        int totalItems = inventoryItems.size();
        int inStockItems = (int) inventoryItems.stream().filter(item -> item.getBalanceQuantity() > 0).count();
        int outOfStockItems = totalItems - inStockItems;
        int lowStockItems = (int) inventoryItems.stream().filter(item -> item.getBalanceQuantity() <= 5 && item.getBalanceQuantity() > 0).count();
        
        BigDecimal totalPurchaseValue = getTotalInventoryPurchaseValue();
        BigDecimal totalSalesValue = getTotalInventorySalesValue();
        BigDecimal potentialProfit = getPotentialProfit();
        
        return new InventorySummary(
            totalItems,
            inStockItems,
            outOfStockItems,
            lowStockItems,
            totalPurchaseValue,
            totalSalesValue,
            potentialProfit
        );
    }
    
    /**
     * Inner class to hold inventory summary data
     */
    public static class InventorySummary {
        private final int totalItems;
        private final int inStockItems;
        private final int outOfStockItems;
        private final int lowStockItems;
        private final BigDecimal totalPurchaseValue;
        private final BigDecimal totalSalesValue;
        private final BigDecimal potentialProfit;
        
        public InventorySummary(int totalItems, int inStockItems, int outOfStockItems, int lowStockItems,
                               BigDecimal totalPurchaseValue, BigDecimal totalSalesValue, BigDecimal potentialProfit) {
            this.totalItems = totalItems;
            this.inStockItems = inStockItems;
            this.outOfStockItems = outOfStockItems;
            this.lowStockItems = lowStockItems;
            this.totalPurchaseValue = totalPurchaseValue;
            this.totalSalesValue = totalSalesValue;
            this.potentialProfit = potentialProfit;
        }
        
        // Getters
        public int getTotalItems() { return totalItems; }
        public int getInStockItems() { return inStockItems; }
        public int getOutOfStockItems() { return outOfStockItems; }
        public int getLowStockItems() { return lowStockItems; }
        public BigDecimal getTotalPurchaseValue() { return totalPurchaseValue; }
        public BigDecimal getTotalSalesValue() { return totalSalesValue; }
        public BigDecimal getPotentialProfit() { return potentialProfit; }
    }
}