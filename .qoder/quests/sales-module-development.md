# Sales Module Development Design

## Overview

This design document outlines the development of a complete Sales module for the JavaFX ERP System. The sales module will enable users to record, manage, and track sales transactions with customers, following the same architectural patterns established by the existing Purchase module.

## Architecture

### Component Structure
The sales module follows the established MVC (Model-View-Controller) architecture pattern:

```mermaid
graph TD
    A[Sales View - sales.fxml] --> B[SalesController.java]
    B --> C[SaleDAO.java]
    B --> D[CustomerDAO.java] 
    B --> E[ItemDAO.java]
    C --> F[Sale.java Model]
    D --> G[Customer.java Model]
    E --> H[Item.java Model]
    I[MainController.java] --> A
    C --> J[(SQLite Database)]
    D --> J
    E --> J
```

### Data Flow Architecture
```mermaid
sequenceDiagram
    participant U as User Interface
    participant SC as SalesController
    participant SD as SaleDAO
    participant CD as CustomerDAO
    participant ID as ItemDAO
    participant DB as Database
    
    U->>SC: User creates/edits sale
    SC->>ID: Load available items
    SC->>CD: Load available customers
    SC->>SC: Validate input data
    SC->>SD: Save/Update sale transaction
    SD->>DB: Execute SQL operations
    DB-->>SD: Return generated ID/confirmation
    SD-->>SC: Return operation result
    SC->>SC: Update observable list
    SC->>U: Refresh table display
```

## UI Implementation

### Sales Management Interface Layout

The sales module UI will mirror the purchasing module structure with sales-specific adaptations:

```mermaid
graph TD
    A[Sales Management View] --> B[Toolbar Section]
    A --> C[Sales Table Section]
    
    B --> D[Search Field]
    B --> E[Add Sale Button]
    B --> F[Edit Sale Button]
    B --> G[Delete Sale Button]
    B --> H[Refresh Button]
    
    C --> I[Sales Table]
    I --> J[Sale ID Column]
    I --> K[Sale Date Column]
    I --> L[Item Name Column]
    I --> M[Customer Name Column]
    I --> N[Quantity Column]
    I --> O[Sales Rate Column]
    I --> P[Total Value Column]
```

### Add/Edit Sale Dialog Structure

```mermaid
graph TD
    A[Sale Dialog] --> B[Customer Selection ComboBox]
    A --> C[Item Selection ComboBox]
    A --> D[Quantity TextField]
    A --> E[Sales Rate TextField]
    A --> F[Sale Date DatePicker]
    A --> G[Total Value Display]
    A --> H[Validation Label]
    A --> I[OK/Cancel Buttons]
    
    B --> J[Customer Display: Name]
    C --> K[Item Display: Name + Category]
    D --> L[Auto-calculation Trigger]
    E --> L
    L --> G
```

### Table Column Configuration
| Column | Property Binding | Cell Factory | Width |
|--------|-----------------|--------------|-------|
| Sale ID | `saleIdProperty()` | Default | 80px |
| Sale Date | `saleDateProperty()` | Date Formatter | 120px |
| Item Name | `itemNameProperty()` | Default | 180px |
| Customer Name | `customerNameProperty()` | Default | 150px |
| Quantity | `quantityProperty()` | Default | 80px |
| Sales Rate | `salesRateProperty()` | Currency Formatter | 100px |
| Total Value | `totalValueProperty()` | Currency Formatter | 120px |

## Controller Implementation

### SalesController Class Structure

```mermaid
classDiagram
    class SalesController {
        -saleDAO: SaleDAO
        -itemDAO: ItemDAO
        -customerDAO: CustomerDAO
        -salesList: ObservableList~Sale~
        -itemsList: ObservableList~Item~
        -customersList: ObservableList~Customer~
        -currentSale: Sale
        
        +initialize(URL, ResourceBundle): void
        +addSale(): void
        +editSale(): void
        +deleteSale(): void
        +refreshData(): void
        +searchSales(): void
        -setupTable(): void
        -setupDialog(): void
        -loadData(): void
        -validateInput(): boolean
        -saveSale(): void
        -updateSale(): void
        -calculateTotalValue(): void
        -clearDialogFields(): void
        -populateDialogFields(Sale): void
    }
    
    SalesController --> SaleDAO
    SalesController --> ItemDAO
    SalesController --> CustomerDAO
```

### Event Handling Architecture

```mermaid
stateDiagram-v2
    [*] --> Initialized
    Initialized --> DataLoaded: loadData()
    DataLoaded --> Ready
    
    Ready --> AddingNewSale: addSale()
    AddingNewSale --> ValidatingInput: OK clicked
    ValidatingInput --> SavingSale: validation passed
    ValidatingInput --> AddingNewSale: validation failed
    SavingSale --> Ready: save successful
    
    Ready --> EditingExistingSale: editSale()
    EditingExistingSale --> ValidatingInput: OK clicked
    EditingExistingSale --> Ready: Cancel clicked
    
    Ready --> DeletingSale: deleteSale()
    DeletingSale --> Ready: deletion complete
    
    Ready --> Searching: searchSales()
    Searching --> Ready: search complete
```

## Data Models Integration

### Sale Entity Enhancement
The existing Sale model already provides the necessary structure. Key properties include:

- **saleId**: Auto-generated primary key
- **itemId**: Foreign key to Items table
- **customerId**: Foreign key to Customers table  
- **quantity**: Number of units sold
- **salesRate**: Unit price charged
- **totalValue**: Calculated total (quantity × salesRate)
- **saleDate**: Transaction date
- **itemName/customerName**: Display properties for UI binding

### Automatic Calculation Logic
```mermaid
graph TD
    A[User Input Change] --> B{Quantity or Rate Changed?}
    B -->|Yes| C[Validate Numeric Input]
    C --> D{Valid Numbers?}
    D -->|Yes| E[Calculate: quantity × salesRate]
    D -->|No| F[Display $0.00]
    E --> G[Update Total Value Label]
    F --> G
    B -->|No| H[No Action]
```

## Database Integration

### SaleDAO Operations
The existing SaleDAO provides complete CRUD operations:

- **insert(Sale)**: Creates new sale record with auto-generated ID
- **update(Sale)**: Updates existing sale record
- **delete(Integer)**: Removes sale record by ID
- **findAll()**: Retrieves all sales with customer/item names
- **searchSales(String)**: Searches sales by criteria

### Search Implementation Strategy
```mermaid
graph TD
    A[Search Input] --> B{Search Term Empty?}
    B -->|Yes| C[Load All Sales]
    B -->|No| D[Execute Search Query]
    D --> E[JOIN with Items and Customers tables]
    E --> F[Filter by multiple criteria]
    F --> G[Return matching results]
    C --> H[Update Table Display]
    G --> H
```

## Form Validation Rules

### Input Validation Matrix
| Field | Validation Rule | Error Message |
|-------|----------------|---------------|
| Customer | Must be selected | "Please select a customer" |
| Item | Must be selected | "Please select an item" |
| Quantity | Integer > 0 | "Quantity must be greater than 0" |
| Sales Rate | Decimal > 0 | "Sales rate must be greater than 0" |
| Sale Date | Must be selected | "Please select a sale date" |

### Real-time Validation Flow
```mermaid
sequenceDiagram
    participant U as User Input
    participant V as Validator
    participant UI as UI Components
    
    U->>V: Field value change
    V->>V: Validate field rules
    alt Validation Success
        V->>UI: Clear error indicators
        V->>UI: Enable OK button
    else Validation Failed
        V->>UI: Show error message
        V->>UI: Disable OK button
        V->>UI: Highlight invalid field
    end
```

## User Interface Specifications

### ComboBox Configuration
#### Customer ComboBox
- **Display Format**: Customer name only
- **Prompt Text**: "Select customer"
- **Cell Factory**: Custom ListCell showing customer name
- **Selection Handling**: Automatic form validation trigger

#### Item ComboBox  
- **Display Format**: "Item Name (Category)"
- **Prompt Text**: "Select item"
- **Cell Factory**: Custom ListCell showing formatted item info
- **Selection Handling**: Auto-populate suggested sales rate if available

### Button State Management
```mermaid
stateDiagram-v2
    [*] --> NoSelection
    NoSelection --> HasSelection: row selected
    HasSelection --> NoSelection: selection cleared
    
    NoSelection: Edit=disabled, Delete=disabled
    HasSelection: Edit=enabled, Delete=enabled
```

### Dialog Window Specifications
- **Title**: "Add New Sale" / "Edit Sale"  
- **Size**: Auto-resize based on content
- **Layout**: GridPane with consistent spacing (15px)
- **Padding**: 20px all sides
- **Theme Integration**: Inherits current application theme
- **Modal**: Blocks interaction with main window

## Error Handling Strategy

### Database Error Management
```mermaid
flowchart TD
    A[Database Operation] --> B{Operation Successful?}
    B -->|Yes| C[Update UI State]
    B -->|No| D[Catch SQLException]
    D --> E[Log Error Details]
    E --> F[Show User-Friendly Alert]
    F --> G[Maintain Current UI State]
    C --> H[Show Success Message]
```

### User Input Error Handling
- **Real-time Validation**: Immediate feedback on field changes
- **Comprehensive Error Display**: Single validation label showing all errors
- **Field Highlighting**: Visual indication of invalid fields
- **Graceful Degradation**: Prevent form submission with invalid data

## Testing Strategy

### Unit Testing Requirements
- **Controller Methods**: Test all CRUD operations
- **Validation Logic**: Test all validation rules and edge cases  
- **Data Binding**: Verify proper model-view synchronization
- **Error Scenarios**: Test database connection failures and invalid inputs

### User Interface Testing
- **Form Interactions**: Test all button clicks and field inputs
- **Table Operations**: Verify sorting, selection, and display
- **Dialog Behavior**: Test modal operation and data persistence
- **Theme Compatibility**: Ensure proper styling in both light and dark themes

### Integration Testing Scenarios
```mermaid
graph TD
    A[User Creates Sale] --> B[Validate All Fields]
    B --> C[Save to Database]
    C --> D[Update Inventory Calculations]
    D --> E[Refresh Table Display]
    E --> F[Verify Data Persistence]
    
    G[User Deletes Sale] --> H[Show Confirmation Dialog]
    H --> I[Remove from Database]
    I --> J[Update UI Lists]
    J --> K[Verify Inventory Update]
```