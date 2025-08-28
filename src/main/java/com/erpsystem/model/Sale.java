package com.erpsystem.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sale entity class representing sales transactions
 */
public class Sale {
    
    private final IntegerProperty saleId;
    private final IntegerProperty itemId;
    private final IntegerProperty quantity;
    private final IntegerProperty customerId;
    private final ObjectProperty<BigDecimal> salesRate;
    private final ObjectProperty<BigDecimal> totalValue;
    private final ObjectProperty<LocalDate> saleDate;
    private final ObjectProperty<LocalDateTime> createdDate;
    
    // For display purposes - these will be set from joins
    private final StringProperty itemName;
    private final StringProperty customerName;
    
    // Default constructor
    public Sale() {
        this(0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now());
    }
    
    // Constructor with parameters
    public Sale(int saleId, int itemId, int quantity, int customerId, 
               BigDecimal salesRate, BigDecimal totalValue, LocalDate saleDate) {
        this.saleId = new SimpleIntegerProperty(saleId);
        this.itemId = new SimpleIntegerProperty(itemId);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.salesRate = new SimpleObjectProperty<>(salesRate);
        this.totalValue = new SimpleObjectProperty<>(totalValue);
        this.saleDate = new SimpleObjectProperty<>(saleDate);
        this.createdDate = new SimpleObjectProperty<>(LocalDateTime.now());
        
        // Initialize display properties
        this.itemName = new SimpleStringProperty("");
        this.customerName = new SimpleStringProperty("");
    }
    
    // Sale ID Property
    public IntegerProperty saleIdProperty() {
        return saleId;
    }
    
    public int getSaleId() {
        return saleId.get();
    }
    
    public void setSaleId(int saleId) {
        this.saleId.set(saleId);
    }
    
    // Item ID Property
    public IntegerProperty itemIdProperty() {
        return itemId;
    }
    
    public int getItemId() {
        return itemId.get();
    }
    
    public void setItemId(int itemId) {
        this.itemId.set(itemId);
    }
    
    // Quantity Property
    public IntegerProperty quantityProperty() {
        return quantity;
    }
    
    public int getQuantity() {
        return quantity.get();
    }
    
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        // Recalculate total value when quantity changes
        if (salesRate.get() != null) {
            updateTotalValue();
        }
    }
    
    // Customer ID Property
    public IntegerProperty customerIdProperty() {
        return customerId;
    }
    
    public int getCustomerId() {
        return customerId.get();
    }
    
    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }
    
    // Sales Rate Property
    public ObjectProperty<BigDecimal> salesRateProperty() {
        return salesRate;
    }
    
    public BigDecimal getSalesRate() {
        return salesRate.get();
    }
    
    public void setSalesRate(BigDecimal salesRate) {
        this.salesRate.set(salesRate);
        updateTotalValue();
    }
    
    // Total Value Property
    public ObjectProperty<BigDecimal> totalValueProperty() {
        return totalValue;
    }
    
    public BigDecimal getTotalValue() {
        return totalValue.get();
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue.set(totalValue);
    }
    
    // Sale Date Property
    public ObjectProperty<LocalDate> saleDateProperty() {
        return saleDate;
    }
    
    public LocalDate getSaleDate() {
        return saleDate.get();
    }
    
    public void setSaleDate(LocalDate saleDate) {
        this.saleDate.set(saleDate);
    }
    
    // Created Date Property
    public ObjectProperty<LocalDateTime> createdDateProperty() {
        return createdDate;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate.get();
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate.set(createdDate);
    }
    
    // Item Name Property (for display)
    public StringProperty itemNameProperty() {
        return itemName;
    }
    
    public String getItemName() {
        return itemName.get();
    }
    
    public void setItemName(String itemName) {
        this.itemName.set(itemName);
    }
    
    // Customer Name Property (for display)
    public StringProperty customerNameProperty() {
        return customerName;
    }
    
    public String getCustomerName() {
        return customerName.get();
    }
    
    public void setCustomerName(String customerName) {
        this.customerName.set(customerName);
    }
    
    // Helper method to update total value
    private void updateTotalValue() {
        if (salesRate.get() != null && quantity.get() > 0) {
            BigDecimal total = salesRate.get().multiply(BigDecimal.valueOf(quantity.get()));
            setTotalValue(total);
        }
    }
    
    // Formatted display methods
    public String getSalesRateDisplay() {
        return "$" + salesRate.get().toString();
    }
    
    public String getTotalValueDisplay() {
        return "$" + totalValue.get().toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sale sale = (Sale) obj;
        return getSaleId() == sale.getSaleId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getSaleId());
    }
}