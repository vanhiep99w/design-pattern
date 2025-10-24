# Template Method Pattern

## Overview

The Template Method pattern is a behavioral design pattern that defines the skeleton of an algorithm in a base class but lets subclasses override specific steps of the algorithm without changing its structure. It promotes code reuse and enforces a consistent workflow across different implementations.

## Pattern Structure

```
┌─────────────────────────────────────┐
│     AbstractClass (Workflow)        │
├─────────────────────────────────────┤
│ + templateMethod() [FINAL]          │  ← Defines the algorithm structure
│ # primitiveOperation1() [ABSTRACT]  │  ← Steps to be implemented
│ # primitiveOperation2() [ABSTRACT]  │
│ # hook1() [DEFAULT]                 │  ← Optional extension points
│ # hook2() [DEFAULT]                 │
└─────────────────────────────────────┘
            △
            │ extends
            │
    ┌───────┴────────┐
    │                │
┌───┴───────────┐  ┌─┴──────────────┐
│ ConcreteClassA│  │ ConcreteClassB │
├───────────────┤  ├────────────────┤
│ Implements    │  │ Implements     │
│ operations    │  │ operations     │
│ differently   │  │ differently    │
└───────────────┘  └────────────────┘
```

## Implementation Examples

This module demonstrates the Template Method pattern through three realistic business scenarios:

### 1. Order Processing Workflow

**Base Class**: `OrderWorkflow`
- **Template Method**: `processOrder()`
- **Abstract Methods**:
  - `validateOrder()` - Validate order details
  - `checkInventory()` - Check product availability
  - `calculatePricing()` - Calculate order pricing
  - `processPayment()` - Process payment
  - `reserveInventory()` - Reserve items
  - `sendConfirmation()` - Send confirmation to customer
- **Hook Methods**:
  - `handlePaymentFailure()` - Handle payment failures (optional override)
  - `afterOrderProcessed()` - Post-processing actions (optional override)

**Concrete Implementations**:
- `StandardOrderWorkflow`: Regular order processing with standard pricing and delivery
- `ExpressOrderWorkflow`: Express orders with priority handling, additional fees, and expedited shipping

**Key Differences**:
- Express orders add a $25 express fee
- Express orders use priority payment gateways
- Express orders generate shipping labels immediately after processing
- Express orders send both SMS and email confirmations

### 2. Payment Processing Workflow

**Base Class**: `PaymentWorkflow`
- **Template Method**: `executePayment()`
- **Abstract Methods**:
  - `validatePaymentDetails()` - Validate payment information
  - `authorizePayment()` - Authorize the transaction
  - `capturePayment()` - Capture/settle the payment
  - `recordTransaction()` - Record transaction in ledger
  - `notifyCustomer()` - Send payment confirmation
- **Hook Methods**:
  - `beforePaymentAuthorization()` - Pre-authorization checks (optional)
  - `handleCaptureFailure()` - Handle capture failures (optional override)
  - `afterPaymentCompleted()` - Post-payment actions (optional override)

**Concrete Implementations**:
- `CreditCardPaymentWorkflow`: Traditional credit card payments with fraud checks and rewards
- `CryptoPaymentWorkflow`: Cryptocurrency payments with blockchain verification

**Key Differences**:
- Credit card workflow performs fraud checks before authorization
- Crypto workflow validates cryptocurrency type (BTC, ETH, USDT)
- Crypto workflow waits for blockchain confirmations (6 confirmations)
- Credit card workflow calculates rewards points after payment
- Crypto workflow generates blockchain explorer links

### 3. Data Import/Export Workflow with JdbcTemplate

**Base Class**: `DataImportExportWorkflow`
- **Template Method**: `executeImport()`
- **Abstract Methods**:
  - `validateInput()` - Validate import parameters
  - `openConnection()` - Open data source connection
  - `readData()` - Read data from source
  - `transformData()` - Transform data format
  - `validateData()` - Validate transformed data
  - `writeData()` - Write data to destination
  - `closeConnection()` - Close connections and cleanup
- **Hook Methods**:
  - `beforeDataProcessing()` - Pre-processing setup (optional)
  - `afterDataProcessing()` - Post-processing actions (optional override)
  - `handleError()` - Error handling (optional override)

**Concrete Implementations**:
- `DatabaseImportWorkflow`: Import data from database using JdbcTemplate
- `CsvImportWorkflow`: Import data from CSV files

**Key Features**:
- **JdbcTemplate Integration**: Uses Spring's JdbcTemplate for database operations
- **H2 Database**: Configured with H2 for testing and development
- **Transaction Support**: Proper connection management and cleanup
- **Error Handling**: Comprehensive error handling with rollback support

## Extension Points (Hook Methods)

Hook methods provide optional customization points without requiring override:

1. **Default Behavior**: Hooks have default implementations that can be used as-is
2. **Optional Override**: Subclasses can override hooks to add specific behavior
3. **Non-Breaking**: Adding new hooks doesn't break existing implementations

### Example Hook Usage:

```java
// Base class defines hook with default behavior
protected void afterOrderProcessed(OrderProcessingContext context) {
    log.debug("Order processing complete");
}

// Subclass can optionally override
@Override
protected void afterOrderProcessed(OrderProcessingContext context) {
    super.afterOrderProcessed(context);
    // Add express-specific post-processing
    generateExpressShippingLabel(context);
    notifyWarehouse(context);
}
```

## Usage Examples

### Order Processing

```java
@Autowired
private StandardOrderWorkflow standardWorkflow;

@Autowired
private ExpressOrderWorkflow expressWorkflow;

public void processOrder(OrderRequest request, boolean express) {
    OrderProcessingContext context = OrderProcessingContext.builder()
        .orderId(request.getOrderId())
        .customerId(request.getCustomerId())
        .items(request.getItems())
        .totalAmount(request.getAmount())
        .build();
    
    OrderWorkflow workflow = express ? expressWorkflow : standardWorkflow;
    OrderProcessingContext result = workflow.processOrder(context);
    
    if (result.isProcessed()) {
        // Order processed successfully
        return result.getConfirmationNumber();
    }
}
```

### Payment Processing

```java
@Autowired
private CreditCardPaymentWorkflow creditCardWorkflow;

@Autowired
private CryptoPaymentWorkflow cryptoWorkflow;

public void processPayment(PaymentRequest request) {
    PaymentContext context = PaymentContext.builder()
        .transactionId(generateTransactionId())
        .customerId(request.getCustomerId())
        .amount(request.getAmount())
        .currency(request.getCurrency())
        .build();
    
    PaymentWorkflow workflow = request.isCrypto() 
        ? cryptoWorkflow 
        : creditCardWorkflow;
        
    PaymentContext result = workflow.executePayment(context);
    
    if (result.isSuccess()) {
        // Payment successful
        return result.getReceiptNumber();
    }
}
```

### Data Import with JdbcTemplate

```java
@Autowired
private DatabaseImportWorkflow dbImportWorkflow;

@Autowired
private CsvImportWorkflow csvImportWorkflow;

public void importCustomers(String source) {
    DataImportExportContext context = DataImportExportContext.builder()
        .operationId(generateOperationId())
        .sourceType(source)
        .destinationType("staging")
        .build();
    
    DataImportExportWorkflow workflow = source.endsWith(".csv")
        ? csvImportWorkflow
        : dbImportWorkflow;
        
    DataImportExportContext result = workflow.executeImport(context);
    
    if (result.isSuccess()) {
        log.info("Imported {} records", result.getRecordsWritten());
    }
}
```

## API Endpoints

### Order Processing
- `POST /api/template-method-demo/order/standard` - Process standard order
- `POST /api/template-method-demo/order/express` - Process express order
- `GET /api/template-method-demo/demo/order-comparison` - Compare order workflows

### Payment Processing
- `POST /api/template-method-demo/payment/credit-card` - Process credit card payment
- `POST /api/template-method-demo/payment/crypto` - Process crypto payment
- `GET /api/template-method-demo/demo/payment-comparison` - Compare payment workflows

### Data Import
- `POST /api/template-method-demo/import/database` - Import from database
- `POST /api/template-method-demo/import/csv` - Import from CSV

## Database Schema

The data import examples use H2 database with the following schema:

```sql
-- Customer source table
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Import staging table
CREATE TABLE import_staging (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Benefits

1. **Code Reuse**: Common algorithm structure is defined once in the base class
2. **Consistency**: All implementations follow the same workflow structure
3. **Flexibility**: Subclasses can customize specific steps without changing the overall algorithm
4. **Maintainability**: Changes to the workflow structure are made in one place
5. **Open/Closed Principle**: Open for extension (new subclasses) but closed for modification (template method is final)

## Pitfalls and Considerations

### 1. Inflexible Inheritance Hierarchy

**Problem**: The Template Method pattern relies on inheritance, which creates tight coupling between base and derived classes.

```java
// This creates a rigid hierarchy
OrderWorkflow (abstract)
    ├── StandardOrderWorkflow
    └── ExpressOrderWorkflow
```

**Impact**:
- Cannot change the algorithm structure without affecting all subclasses
- Difficult to compose behaviors from multiple sources
- Violates "favor composition over inheritance" principle

**Mitigation**:
- Use hook methods for optional behavior
- Consider Strategy pattern for more flexible composition
- Keep inheritance hierarchies shallow (1-2 levels max)

### 2. Template Method Must Be Final

**Problem**: The template method should be `final` to prevent subclasses from changing the algorithm structure.

```java
// CORRECT: Template method is final
public final OrderProcessingContext processOrder(OrderProcessingContext context) {
    validateOrder(context);
    checkInventory(context);
    // ...
}

// WRONG: Template method can be overridden
public OrderProcessingContext processOrder(OrderProcessingContext context) {
    // Subclass could completely bypass the intended workflow
}
```

**Best Practice**: Always mark template methods as `final` in production code.

### 3. Too Many Abstract Methods

**Problem**: Having too many abstract methods makes it difficult to create new subclasses.

**Mitigation**:
- Provide reasonable defaults where possible
- Use hook methods instead of abstract methods for optional behavior
- Consider splitting overly complex workflows into smaller templates

### 4. Hidden Dependencies Between Steps

**Problem**: Steps in the template method may have hidden dependencies on execution order.

```java
// calculatePricing() depends on checkInventory() being called first
protected void calculatePricing(OrderProcessingContext context) {
    if (!context.isInventoryAvailable()) {
        // This assumes checkInventory() was already called
        throw new IllegalStateException("Inventory not checked");
    }
}
```

**Best Practice**:
- Document dependencies clearly
- Validate preconditions in each step
- Use context objects to track workflow state

### 5. Difficult to Test Individual Steps

**Problem**: Since the template method is final and calls multiple abstract methods, testing individual steps can be challenging.

**Mitigation**:
- Make abstract methods protected to allow unit testing
- Use partial mocking when necessary
- Test the full workflow in integration tests

### 6. Performance Overhead

**Problem**: The template method may execute steps that aren't needed for all scenarios.

```java
// All orders go through these steps, even if some are unnecessary
validateOrder(context);        // Always needed
checkInventory(context);       // May not be needed for digital products
calculatePricing(context);     // Always needed
processPayment(context);       // Always needed
reserveInventory(context);     // May not be needed for digital products
```

**Mitigation**:
- Use conditional logic in hook methods
- Consider multiple template methods for significantly different workflows
- Profile and optimize hot paths

## When to Use Template Method Pattern

**Use Template Method when**:
- You have multiple classes with similar algorithms that differ in specific steps
- You want to control the algorithm structure while allowing customization
- You want to avoid code duplication across similar workflows
- You have a clear, stable algorithm structure that won't change frequently

**Avoid Template Method when**:
- The algorithm structure changes frequently
- You need to compose behaviors from multiple sources (use Strategy or Decorator)
- You want runtime flexibility to change the algorithm (use Strategy)
- The inheritance hierarchy becomes too deep or complex

## Comparison with Other Patterns

### Template Method vs Strategy

| Template Method | Strategy |
|----------------|----------|
| Uses inheritance | Uses composition |
| Compile-time selection | Runtime selection |
| Single algorithm with variations | Multiple interchangeable algorithms |
| Less flexible | More flexible |
| Better for stable workflows | Better for dynamic behavior |

### Template Method vs Factory Method

Factory Method is actually a specialized use case of Template Method where the "step" being customized is object creation.

## Testing

The implementation includes comprehensive tests:

- **Unit Tests**: Test individual workflow implementations
- **Integration Tests**: Test with real H2 database using JdbcTemplate
- **Hook Override Tests**: Verify hook methods are called correctly
- **Validation Tests**: Test error handling and validation logic
- **Sequence Tests**: Verify correct execution order of template steps

Run tests with:
```bash
mvn test -Dtest=*TemplateMethod*
```

## Configuration

Database configuration in `application.properties`:

```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:designpatterndb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.defer-datasource-initialization=true
```

## References

- **Design Patterns: Elements of Reusable Object-Oriented Software** (Gang of Four)
- **Head First Design Patterns** by Freeman & Freeman
- **Effective Java** by Joshua Bloch (Item 20: Prefer class hierarchies to tagged classes)
- Spring Framework JdbcTemplate Documentation

## Related Patterns

- **Strategy Pattern**: Alternative for runtime algorithm selection
- **Factory Method**: Specialized Template Method for object creation
- **Hook Method**: Optional extension point within Template Method
- **Adapter Pattern**: Can be used to adapt different data sources in import workflows
