package com.erpsystem.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Item entity class representing item master data
 */
public class Item {
    
    private final IntegerProperty itemId;
    private final StringProperty name;
    private final StringProperty category;
    private final ObjectProperty<LocalDateTime> createdDate;
    private final ObjectProperty<LocalDateTime> updatedDate;
    
    // Default constructor
    public Item() {
        this(0, "", "");
    }
    
    // Constructor with parameters
    public Item(int itemId, String name, String category) {
        this.itemId = new SimpleIntegerProperty(itemId);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.createdDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.updatedDate = new SimpleObjectProperty<>(LocalDateTime.now());
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
    
    // Name Property
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
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
    
    // Updated Date Property
    public ObjectProperty<LocalDateTime> updatedDateProperty() {
        return updatedDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate.get();
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate.set(updatedDate);
    }
    
    @Override
    public String toString() {
        return name.get();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item item = (Item) obj;
        return getItemId() == item.getItemId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getItemId());
    }
}