package com.erpsystem.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Customer entity class representing customer master data
 */
public class Customer {
    
    private final IntegerProperty customerId;
    private final StringProperty name;
    private final StringProperty contact;
    private final StringProperty address;
    private final ObjectProperty<LocalDateTime> createdDate;
    private final ObjectProperty<LocalDateTime> updatedDate;
    
    // Default constructor
    public Customer() {
        this(0, "", "", "");
    }
    
    // Constructor with parameters
    public Customer(int customerId, String name, String contact, String address) {
        this.customerId = new SimpleIntegerProperty(customerId);
        this.name = new SimpleStringProperty(name);
        this.contact = new SimpleStringProperty(contact);
        this.address = new SimpleStringProperty(address);
        this.createdDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.updatedDate = new SimpleObjectProperty<>(LocalDateTime.now());
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
    
    // Contact Property
    public StringProperty contactProperty() {
        return contact;
    }
    
    public String getContact() {
        return contact.get();
    }
    
    public void setContact(String contact) {
        this.contact.set(contact);
    }
    
    // Address Property
    public StringProperty addressProperty() {
        return address;
    }
    
    public String getAddress() {
        return address.get();
    }
    
    public void setAddress(String address) {
        this.address.set(address);
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
        Customer customer = (Customer) obj;
        return getCustomerId() == customer.getCustomerId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getCustomerId());
    }
}