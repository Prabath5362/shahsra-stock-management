# JavaFX ERP Desktop Application

A complete Enterprise Resource Planning (ERP) desktop application built with JavaFX, SQLite, and modern design patterns.

## 🚀 Features

### Master Data Management
- **Suppliers**: Manage supplier information including contact details and addresses
- **Customers**: Maintain customer database with full CRUD operations
- **Items**: Product catalog with purchase rates, sales rates, and categories

### Transaction Processing
- **Purchasing**: Record purchase transactions with automatic value calculations
- **Sales**: Track sales transactions with customer and item details
- **Auto-calculation**: Total values computed as Quantity × Rate

### Inventory Management
- **Real-time Balance**: Balance Qty = SUM(Purchases) - SUM(Sales)
- **Valuation**: Purchase Value = Balance Qty × Purchase Rate
- **Sales Value**: Sales Value = Balance Qty × Sales Rate
- **Low Stock Alerts**: Identify items requiring restock

### Financial Reporting
- **Revenue Tracking**: Total Sales (Money In)
- **Cost Management**: Total Purchases (Money Out)
- **Profit Analysis**: Revenue - Costs
- **Dashboard Metrics**: Key performance indicators with charts

### User Interface
- **Professional Design**: Clean, modern interface with comprehensive dark/light theme support
- **Light Theme Default**: Starts with professional light theme, with full dark theme available
- **Full Screen Experience**: Maximized window with visible controls (minimize, maximize, close)
- **Complete Theme Coverage**: All UI components styled consistently including:
  - Forms, tables, buttons, and navigation
  - Dialogs, alerts, and popup menus
  - Charts, progress indicators, and status elements
  - Scrollbars, separators, and interactive controls
  - Sidebar, header, and status bar properly switch themes
- **Dashboard**: Visual charts and key metrics overview
- **Sidebar Navigation**: Easy access to all modules with professional styling
- **Data Tables**: Sortable, searchable data grids with enhanced readability
- **Form Validation**: Input validation and error handling with consistent styling
- **Window Management**: Proper fullscreen handling with accessible window controls

## 🛠️ Technology Stack

- **Frontend**: JavaFX 20 with FXML
- **Backend**: Java 17
- **Database**: SQLite with JDBC
- **Build Tool**: Maven
- **Architecture**: MVC pattern with DAO layer
- **Styling**: CSS with theme support

## 📁 Project Structure

```
ERPSystem/
├── src/
│   ├── main/
│   │   ├── java/com/erpsystem/
│   │   │   ├── MainApplication.java          # Main application entry point
│   │   │   ├── controller/                   # UI Controllers
│   │   │   │   ├── MainController.java       # Main layout controller
│   │   │   │   ├── DashboardController.java  # Dashboard controller
│   │   │   │   └── SuppliersController.java  # Suppliers CRUD controller
│   │   │   ├── model/                        # Entity classes
│   │   │   │   ├── Supplier.java
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Item.java
│   │   │   │   ├── Purchase.java
│   │   │   │   ├── Sale.java
│   │   │   │   └── InventoryItem.java
│   │   │   ├── dao/                          # Data Access Objects
│   │   │   │   ├── BaseDAO.java
│   │   │   │   ├── SupplierDAO.java
│   │   │   │   ├── CustomerDAO.java
│   │   │   │   ├── ItemDAO.java
│   │   │   │   ├── PurchaseDAO.java
│   │   │   │   └── SaleDAO.java
│   │   │   ├── service/                      # Business Logic
│   │   │   │   ├── InventoryService.java
│   │   │   │   └── FinanceService.java
│   │   │   └── util/                         # Utilities
│   │   │       └── DatabaseUtil.java
│   │   └── resources/
│   │       ├── fxml/                         # UI Layouts
│   │       │   ├── main-layout.fxml
│   │       │   ├── dashboard.fxml
│   │       │   └── suppliers.fxml
│   │       ├── css/                          # Stylesheets
│   │       │   ├── theme-light.css
│   │       │   └── theme-dark.css
│   │       └── database_schema.sql           # Database schema
├── pom.xml                                   # Maven configuration
└── README.md                                 # This file
```

## 🔧 Prerequisites

- **Java 17** or later
- **JavaFX 20** or later
- **Maven 3.6** or later
- **SQLite** (embedded, no separate installation needed)

## 🚀 Quick Start

### 1. Clone or Download
Download the project files to your local machine.

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn javafx:run
```

Alternatively, if Maven is not available:
```bash
# Compile
javac -cp "path/to/javafx/lib/*:path/to/sqlite-jdbc.jar" src/main/java/com/erpsystem/*.java src/main/java/com/erpsystem/**/*.java

# Run (adjust paths according to your system)
java -cp ".:path/to/javafx/lib/*:path/to/sqlite-jdbc.jar" --module-path path/to/javafx/lib --add-modules javafx.controls,javafx.fxml com.erpsystem.MainApplication
```

### 4. Database Setup
The application automatically:
- Creates the SQLite database file (`erp_system.db`)
- Initializes tables with proper schema
- Inserts sample data for testing

## 📊 Sample Data

The application includes sample data to demonstrate functionality:

### Suppliers
- ABC Supplies Ltd
- Tech Components Inc
- Global Materials

### Customers
- Retail Store A
- Corporate Client B
- Online Marketplace C

### Items
- Laptop Computer ($800 purchase / $1200 sales)
- Office Chair ($150 purchase / $250 sales)
- Printer Paper ($25 purchase / $40 sales)
- USB Drive 32GB ($15 purchase / $30 sales)
- Desk Lamp ($45 purchase / $75 sales)

### Sample Transactions
Includes pre-loaded purchase and sales transactions to demonstrate:
- Inventory calculations
- Financial reporting
- Dashboard metrics

## 💡 Usage Guide

### Dashboard
- View key business metrics at a glance
- Monitor total sales, purchases, and profit
- Check inventory status and alerts
- Access quick actions for common tasks

### Suppliers Management (Fully Implemented)
- **Add**: Click "Add Supplier" to create new supplier records
- **Edit**: Double-click any row or select and click "Edit"
- **Delete**: Select a supplier and click "Delete" (with confirmation)
- **Search**: Use the search box to filter suppliers by name

### Navigation
- Use the sidebar to switch between modules
- Click the theme toggle (🌙/☀️) to switch between dark and light themes
- Status bar shows database connection status

### Data Entry Best Practices
- Required fields are marked with asterisk (*)
- Form validation provides immediate feedback
- All monetary values calculated automatically
- Date fields use standard date picker controls

## 🏗️ Architecture Overview

### Model-View-Controller (MVC)
- **Model**: Entity classes with JavaFX properties for data binding
- **View**: FXML files defining UI layouts
- **Controller**: Handle user interactions and business logic

### Data Access Object (DAO) Pattern
- Centralized database operations
- Consistent interface across all entities
- Connection pooling and resource management

### Service Layer
- Business logic separation
- Complex calculations (inventory, finance)
- Cross-entity operations and reporting

### Enhanced Theme System
- **Professional Light Theme**: Clean, modern light theme with proper contrast and readability (default)
- **Professional Dark Theme**: GitHub-inspired dark theme available via toggle
- **Comprehensive Coverage**: All JavaFX components styled consistently
- **Perfect Theme Switching**: Sidebar, header, and all elements properly switch themes
- **CSS-Based Architecture**: FXML uses CSS classes instead of inline styles
- **Dialog Integration**: Alerts and dialogs automatically inherit current theme
- **Color Harmony**: Carefully selected color palette for professional appearance
- **Accessibility**: Proper contrast ratios and readable typography
- **Component Coverage**: 
  - Navigation, buttons, forms, and tables
  - Dialogs, menus, and context menus
  - Charts, progress bars, and status indicators
  - Scrollbars, tabs, checkboxes, and sliders
  - All interactive elements with hover and focus states

## 🔧 Customization

### Adding New Entities
1. Create model class in `model/` package
2. Implement DAO class extending `BaseDAO`
3. Add service methods if needed
4. Create FXML and controller
5. Update navigation in `MainController`

### Modifying Calculations
- Edit `InventoryService` for inventory-related calculations
- Edit `FinanceService` for financial calculations
- Update dashboard queries in `DashboardController`

### Styling Changes
- Modify `theme-light.css` or `theme-dark.css`
- Use CSS custom properties for consistent theming
- Apply styles using CSS classes in FXML

## 📈 Planned Enhancements

### Immediate (Phase 2)
- Complete implementation of Customers, Items, Purchasing, Sales modules
- Advanced search and filtering capabilities
- Data export functionality (CSV, PDF)
- Backup and restore features

### Future (Phase 3)
- Multi-user support with authentication
- Advanced reporting and analytics
- Barcode scanning integration
- REST API for integration
- Multi-language support

## 🐛 Troubleshooting

### Common Issues

**Database Connection Failed**
- Check if SQLite JDBC driver is in classpath
- Ensure write permissions in application directory

**UI Not Loading**
- Verify FXML files are in `src/main/resources/fxml/`
- Check CSS files are in `src/main/resources/css/`
- Ensure JavaFX modules are properly configured

**Charts Not Displaying**
- Verify sample data exists in database
- Check console for any calculation errors
- Ensure JavaFX Controls module is loaded

**Theme Not Switching**
- Check CSS files are accessible
- Verify theme toggle button functionality
- Clear any cached stylesheets

### Development Mode
For development and debugging:
- Enable console logging in `DatabaseUtil`
- Use JavaFX Scene Builder for FXML editing
- Monitor database file (`erp_system.db`) with SQLite tools

## 📝 License

This project is created as a boilerplate/template for ERP systems. Feel free to modify and distribute according to your needs.

## 🤝 Contributing

This is a boilerplate project designed to be extended. Key areas for contribution:
- Complete remaining CRUD modules
- Enhanced reporting capabilities
- Performance optimizations
- Additional themes and styling
- Documentation improvements

## 📞 Support

For questions or issues:
1. Check the troubleshooting section above
2. Review console logs for error details
3. Verify database schema and sample data
4. Check JavaFX and dependency versions

---

**Built with ❤️ using JavaFX and modern design patterns**