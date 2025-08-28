package com.erpsystem.test;

import com.erpsystem.util.DatabaseUtil;

public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection and initialization...");
        
        try {
            boolean result = DatabaseUtil.testConnection();
            System.out.println("Database test result: " + result);
        } catch (Exception e) {
            System.out.println("Exception occurred:");
            e.printStackTrace();
        }
        
        System.out.println("Test completed.");
    }
}