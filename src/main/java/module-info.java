module erpsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    
    exports com.erpsystem;
    exports com.erpsystem.controller;
    exports com.erpsystem.model;
    exports com.erpsystem.dao;
    exports com.erpsystem.service;
    exports com.erpsystem.util;
    
    opens com.erpsystem.controller to javafx.fxml;
    opens com.erpsystem.model to javafx.base;
}