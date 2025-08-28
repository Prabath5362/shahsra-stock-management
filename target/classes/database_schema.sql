-- ERP System Database Schema
-- SQLite Database Creation Script

-- Create Suppliers table
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(100),
    address TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create Customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(100),
    address TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create Items table
CREATE TABLE IF NOT EXISTS items (
    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create Purchases table
CREATE TABLE IF NOT EXISTS purchases (
    purchase_id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    supplier_id INTEGER NOT NULL,
    purchase_rate DECIMAL(10,2) NOT NULL,
    total_value DECIMAL(10,2) NOT NULL,
    purchase_date DATE NOT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(item_id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);

-- Create Sales table
CREATE TABLE IF NOT EXISTS sales (
    sale_id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    customer_id INTEGER NOT NULL,
    sales_rate DECIMAL(10,2) NOT NULL,
    total_value DECIMAL(10,2) NOT NULL,
    sale_date DATE NOT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(item_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_purchases_item_id ON purchases(item_id);
CREATE INDEX IF NOT EXISTS idx_purchases_supplier_id ON purchases(supplier_id);
CREATE INDEX IF NOT EXISTS idx_purchases_date ON purchases(purchase_date);

CREATE INDEX IF NOT EXISTS idx_sales_item_id ON sales(item_id);
CREATE INDEX IF NOT EXISTS idx_sales_customer_id ON sales(customer_id);
CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(sale_date);

-- Insert sample data for testing
-- INSERT OR IGNORE INTO suppliers (supplier_id, name, contact, address) VALUES
-- (1, 'ABC Supplies Ltd', 'John Smith - 555-0101', '123 Main St, Business District'),
-- (2, 'Tech Components Inc', 'Sarah Johnson - 555-0102', '456 Tech Ave, Industrial Zone'),
-- (3, 'Global Materials', 'Mike Wilson - 555-0103', '789 Supply Road, Warehouse District');

-- INSERT OR IGNORE INTO customers (customer_id, name, contact, address) VALUES
-- (1, 'Retail Store A', 'Alice Brown - 555-0201', '321 Retail Blvd, Shopping Center'),
-- (2, 'Corporate Client B', 'Bob Davis - 555-0202', '654 Corporate Dr, Business Park'),
-- (3, 'Online Marketplace C', 'Carol White - 555-0203', '987 Digital St, E-commerce Zone');

INSERT OR IGNORE INTO items (item_id, name, category) VALUES
(1, 'Laptop Computer', 'Electronics'),
(2, 'Office Chair', 'Furniture'),
(3, 'Printer Paper', 'Stationery'),
(4, 'USB Drive 32GB', 'Electronics'),
(5, 'Desk Lamp', 'Furniture');

-- Insert sample purchase transactions
-- Sample purchase data commented out since supplier sample data was removed
-- INSERT OR IGNORE INTO purchases (item_id, quantity, supplier_id, purchase_rate, total_value, purchase_date) VALUES
-- (1, 10, 2, 800.00, 8000.00, '2024-01-15'),
-- (2, 25, 1, 150.00, 3750.00, '2024-01-16'),
-- (3, 100, 3, 25.00, 2500.00, '2024-01-17'),
-- (4, 50, 2, 15.00, 750.00, '2024-01-18'),
-- (5, 20, 1, 45.00, 900.00, '2024-01-19');

-- Insert sample sales transactions
-- Sample sales data commented out since it depends on purchase data
-- INSERT OR IGNORE INTO sales (item_id, quantity, customer_id, sales_rate, total_value, sale_date) VALUES
-- (1, 5, 2, 1200.00, 6000.00, '2024-01-20'),
-- (2, 15, 1, 250.00, 3750.00, '2024-01-21'),
-- (3, 40, 3, 40.00, 1600.00, '2024-01-22'),
-- (4, 30, 1, 30.00, 900.00, '2024-01-23'),
-- (5, 12, 2, 75.00, 900.00, '2024-01-24');