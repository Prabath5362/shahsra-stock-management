package com.erpsystem.model;

import javafx.beans.property.*;
import java.math.BigDecimal;

/**
 * InventoryItem class representing calculated inventory data for items
 */
public class InventoryItem {
    
    private final IntegerProperty itemId;
    private final StringProperty itemName;
    private final StringProperty category;
    private final IntegerProperty balanceQuantity;
    private final ObjectProperty<BigDecimal> purchaseRate;
    private final ObjectProperty<BigDecimal> salesRate;
    private final ObjectProperty<BigDecimal> purchaseValue;
    private final ObjectProperty<BigDecimal> salesValue;
    
    // Default constructor
    public InventoryItem() {
        this(0, "", "", 0, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    
    // Constructor with parameters
    public InventoryItem(int itemId, String itemName, String category, 
                        int balanceQuantity, BigDecimal purchaseRate, BigDecimal salesRate) {
        this.itemId = new SimpleIntegerProperty(itemId);
        this.itemName = new SimpleStringProperty(itemName);
        this.category = new SimpleStringProperty(category);
        this.balanceQuantity = new SimpleIntegerProperty(balanceQuantity);
        this.purchaseRate = new SimpleObjectProperty<>(purchaseRate);
        this.salesRate = new SimpleObjectProperty<>(salesRate);
        
        // Calculate values
        this.purchaseValue = new SimpleObjectProperty<>(
            purchaseRate.multiply(BigDecimal.valueOf(balanceQuantity))
        );
        this.salesValue = new SimpleObjectProperty<>(
            salesRate.multiply(BigDecimal.valueOf(balanceQuantity))
        );
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
    
    // Item Name Property
    public StringProperty itemNameProperty() {
        return itemName;
    }
    
    public String getItemName() {
        return itemName.get();
    }
    
    public void setItemName(String itemName) {
        this.itemName.set(itemName);
    }
    
    // Category Property
    public StringProperty categoryProperty() {
        return category;
    }
    
    public String getCategory() {
        return category.get();
    }
    
    public void setCategory(String category) {
        this.category.set(category);
    }
    
    // Balance Quantity Property
    public IntegerProperty balanceQuantityProperty() {
        return balanceQuantity;
    }
    
    public int getBalanceQuantity() {
        return balanceQuantity.get();
    }
    
    public void setBalanceQuantity(int balanceQuantity) {
        this.balanceQuantity.set(balanceQuantity);
        updateValues();
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
        updateValues();
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
        updateValues();
    }
    
    // Purchase Value Property
    public ObjectProperty<BigDecimal> purchaseValueProperty() {
        return purchaseValue;
    }
    
    public BigDecimal getPurchaseValue() {
        return purchaseValue.get();
    }
    
    public void setPurchaseValue(BigDecimal purchaseValue) {
        this.purchaseValue.set(purchaseValue);
    }
    
    // Sales Value Property
    public ObjectProperty<BigDecimal> salesValueProperty() {
        return salesValue;
    }
    
    public BigDecimal getSalesValue() {
        return salesValue.get();
    }
    
    public void setSalesValue(BigDecimal salesValue) {
        this.salesValue.set(salesValue);
    }
    
    // Update calculated values when base values change
    private void updateValues() {
        if (purchaseRate.get() != null && balanceQuantity.get() != 0) {
            setPurchaseValue(purchaseRate.get().multiply(BigDecimal.valueOf(balanceQuantity.get())));
        } else {
            setPurchaseValue(BigDecimal.ZERO);
        }
        
        if (salesRate.get() != null && balanceQuantity.get() != 0) {
            setSalesValue(salesRate.get().multiply(BigDecimal.valueOf(balanceQuantity.get())));
        } else {
            setSalesValue(BigDecimal.ZERO);
        }
    }
    
    // Formatted display methods
    public String getPurchaseRateDisplay() {
        return "$" + purchaseRate.get().toString();
    }
    
    public String getSalesRateDisplay() {
        return "$" + salesRate.get().toString();
    }
    
    public String getPurchaseValueDisplay() {
        return "$" + purchaseValue.get().toString();
    }
    
    public String getSalesValueDisplay() {
        return "$" + salesValue.get().toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InventoryItem item = (InventoryItem) obj;
        return getItemId() == item.getItemId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getItemId());
    }
}