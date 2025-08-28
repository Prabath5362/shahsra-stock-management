package com.erpsystem.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Purchase entity class representing purchase transactions
 */
public class Purchase {
    
    private final IntegerProperty purchaseId;
    private final IntegerProperty itemId;
    private final IntegerProperty quantity;
    private final IntegerProperty supplierId;
    private final ObjectProperty<BigDecimal> purchaseRate;
    private final ObjectProperty<BigDecimal> totalValue;
    private final ObjectProperty<LocalDate> purchaseDate;
    private final ObjectProperty<LocalDateTime> createdDate;
    
    // For display purposes - these will be set from joins
    private final StringProperty itemName;
    private final StringProperty supplierName;
    
    // Default constructor
    public Purchase() {
        this(0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now());
    }
    
    // Constructor with parameters
    public Purchase(int purchaseId, int itemId, int quantity, int supplierId, 
                   BigDecimal purchaseRate, BigDecimal totalValue, LocalDate purchaseDate) {
        this.purchaseId = new SimpleIntegerProperty(purchaseId);
        this.itemId = new SimpleIntegerProperty(itemId);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.supplierId = new SimpleIntegerProperty(supplierId);
        this.purchaseRate = new SimpleObjectProperty<>(purchaseRate);
        this.totalValue = new SimpleObjectProperty<>(totalValue);
        this.purchaseDate = new SimpleObjectProperty<>(purchaseDate);
        this.createdDate = new SimpleObjectProperty<>(LocalDateTime.now());
        
        // Initialize display properties
        this.itemName = new SimpleStringProperty("");
        this.supplierName = new SimpleStringProperty("");
    }
    
    // Purchase ID Property
    public IntegerProperty purchaseIdProperty() {
        return purchaseId;
    }
    
    public int getPurchaseId() {
        return purchaseId.get();
    }
    
    public void setPurchaseId(int purchaseId) {
        this.purchaseId.set(purchaseId);
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
        if (purchaseRate.get() != null) {
            updateTotalValue();
        }
    }
    
    // Supplier ID Property
    public IntegerProperty supplierIdProperty() {
        return supplierId;
    }
    
    public int getSupplierId() {
        return supplierId.get();
    }
    
    public void setSupplierId(int supplierId) {
        this.supplierId.set(supplierId);
    }
    
    // Purchase Rate Property
    public ObjectProperty<BigDecimal> purchaseRateProperty() {
        return purchaseRate;
    }
    
    public BigDecimal getPurchaseRate() {
        return purchaseRate.get();
    }
    
    public void setPurchaseRate(BigDecimal purchaseRate) {
        this.purchaseRate.set(purchaseRate);
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
    
    // Purchase Date Property
    public ObjectProperty<LocalDate> purchaseDateProperty() {
        return purchaseDate;
    }
    
    public LocalDate getPurchaseDate() {
        return purchaseDate.get();
    }
    
    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate.set(purchaseDate);
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
    
    // Supplier Name Property (for display)
    public StringProperty supplierNameProperty() {
        return supplierName;
    }
    
    public String getSupplierName() {
        return supplierName.get();
    }
    
    public void setSupplierName(String supplierName) {
        this.supplierName.set(supplierName);
    }
    
    // Helper method to update total value
    private void updateTotalValue() {
        if (purchaseRate.get() != null && quantity.get() > 0) {
            BigDecimal total = purchaseRate.get().multiply(BigDecimal.valueOf(quantity.get()));
            setTotalValue(total);
        }
    }
    
    // Formatted display methods
    public String getPurchaseRateDisplay() {
        return "$" + purchaseRate.get().toString();
    }
    
    public String getTotalValueDisplay() {
        return "$" + totalValue.get().toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Purchase purchase = (Purchase) obj;
        return getPurchaseId() == purchase.getPurchaseId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getPurchaseId());
    }
}