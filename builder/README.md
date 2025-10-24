# Builder Pattern Examples

## Overview

The Builder pattern is a creational design pattern that separates the construction of a complex object from its representation, allowing the same construction process to create various representations. This pattern is particularly useful when dealing with objects that have many optional parameters or require multiple steps to construct.

## Table of Contents

1. [Key Concepts](#key-concepts)
2. [Advantages](#advantages)
3. [When to Use Builder Pattern](#when-to-use-builder-pattern)
4. [Implementation Approaches](#implementation-approaches)
5. [Examples in This Package](#examples-in-this-package)
6. [Manual vs Lombok Builder](#manual-vs-lombok-builder)
7. [Common Pitfalls](#common-pitfalls)
8. [Best Practices](#best-practices)

---

## Key Concepts

### Fluent Interface
Builders typically implement a **fluent interface** (method chaining) that allows for readable and expressive object construction:

```java
Invoice invoice = Invoice.builder()
    .invoiceNumber("INV-001")
    .customerName("John Doe")
    .customerEmail("john@example.com")
    .items(items)
    .taxRate(new BigDecimal("0.10"))
    .build();
```

### Immutability
Builders often create **immutable objects** where all fields are final and can only be set during construction:

```java
public final class Invoice {
    private final String invoiceNumber;
    private final String customerName;
    // ... other final fields
}
```

### Validation
Builders provide a central place for **validation logic** before object creation:

```java
public Invoice build() {
    validate();      // Check all required fields
    calculateAmounts(); // Compute derived values
    return new Invoice(this);
}
```

---

## Advantages

### 1. **Readability and Maintainability**
```java
// Without Builder - telescoping constructor
Order order = new Order(1L, items, total, status, "notes", "address", "payment", delivery);

// With Builder - clear and readable
OrderDetails order = OrderBuilder.newOrder()
    .forUser(1L)
    .addItem("Laptop", 1, new BigDecimal("999.99"))
    .shippingTo("123 Main St")
    .payingWith("Credit Card")
    .withNotes("Handle with care")
    .build();
```

### 2. **Flexibility with Optional Parameters**
```java
// Minimal registration
UserRegistration basic = UserRegistration.builder()
    .username("johndoe")
    .email("john@example.com")
    .password("password123")
    .firstName("John")
    .lastName("Doe")
    .termsAccepted(true)
    .build();

// Full registration with optional fields
UserRegistration full = UserRegistration.builder()
    .username("johndoe")
    .email("john@example.com")
    .password("password123")
    .firstName("John")
    .lastName("Doe")
    .phoneNumber("555-1234")
    .address("123 Main St")
    .city("Springfield")
    .termsAccepted(true)
    .marketingOptIn(true)
    .build();
```

### 3. **Validation and Business Logic**
Centralized validation ensures objects are always in a valid state:

```java
private void validate() {
    if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
        throw new IllegalStateException("Invoice number is required");
    }
    if (dueDate.isBefore(issueDate)) {
        throw new IllegalStateException("Due date cannot be before issue date");
    }
}
```

### 4. **Step-by-Step Construction**
Builders enable complex, multi-step object construction:

```java
OrderBuilder builder = OrderBuilder.newOrder()
    .forUser(1L);

// Add items dynamically
for (CartItem item : cart.getItems()) {
    builder.addItem(item.getName(), item.getQuantity(), item.getPrice());
}

// Check total before proceeding
if (builder.getCurrentTotal().compareTo(maxAmount) > 0) {
    throw new IllegalStateException("Order exceeds maximum amount");
}

OrderDetails order = builder.build();
```

---

## When to Use Builder Pattern

### ✅ Use Builder When:

1. **Multiple constructor parameters** (especially 4+ parameters)
2. **Many optional parameters**
3. **Object requires validation** before use
4. **Complex construction logic** (calculations, transformations)
5. **Need for immutable objects**
6. **Step-by-step object construction** is beneficial
7. **Creating different representations** of the same object type

### ❌ Avoid Builder When:

1. **Simple objects** with few fields (1-3 required fields)
2. **No optional parameters**
3. **Standard constructors suffice**
4. **Performance is critical** (builder adds overhead)
5. **Object is mutable by design**

---

## Implementation Approaches

### 1. Manual Builder (Full Control)

**Example:** `Invoice`, `OrderBuilder`

```java
public final class Invoice {
    private final String invoiceNumber;
    // ... other fields
    
    private Invoice(Builder builder) {
        this.invoiceNumber = builder.invoiceNumber;
        // ... copy other fields
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private String invoiceNumber;
        // ... other fields
        
        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }
        
        public Invoice build() {
            validate();
            return new Invoice(this);
        }
    }
}
```

**Advantages:**
- Full control over builder behavior
- Custom validation logic
- Can perform calculations in `build()`
- Can have custom builder methods (e.g., `addItem()`)

**Disadvantages:**
- More boilerplate code
- Must maintain builder manually
- More code to test

### 2. Lombok @Builder (Less Boilerplate)

**Example:** `UserRegistration`

```java
@Getter
@Builder
public class UserRegistration {
    private final String username;
    private final String email;
    @Builder.Default
    private final boolean termsAccepted = false;
    
    public void validate() {
        // Validation logic
    }
}
```

**Advantages:**
- Minimal boilerplate
- Automatic builder generation
- Less code to maintain
- `@Builder.Default` for default values

**Disadvantages:**
- Less control over builder behavior
- Validation must be called separately after `build()`
- Cannot perform calculations during build
- Limited customization options

### 3. Fluent Builder (Advanced Chaining)

**Example:** `QueryBuilder`

```java
Query query = QueryBuilder.select("id", "name", "email")
    .from("users")
    .where("active", "=", true)
    .andWhere("role", "=", "ADMIN")
    .orderBy("name", "ASC")
    .limit(10)
    .build();
```

**Advantages:**
- Highly expressive DSL-like API
- Great for complex queries/commands
- Natural language-like syntax
- Self-documenting code

---

## Examples in This Package

### 1. Invoice Builder (Manual - Complex Domain Object)

**Location:** `com.designpatterns.showcase.builder.domain.Invoice`

**Features:**
- Immutable invoice with items
- Automatic calculation of subtotals, taxes, discounts
- Comprehensive validation
- Default values (issue date, due date, payment terms)

**Example:**
```java
Invoice invoice = Invoice.builder()
    .invoiceNumber("INV-2024-001")
    .customerName("Acme Corporation")
    .customerEmail("billing@acme.com")
    .customerAddress("123 Business Rd, Suite 100")
    .items(items)
    .taxRate(new BigDecimal("0.08"))
    .discountAmount(new BigDecimal("50.00"))
    .paymentTerms("Net 30")
    .build();
```

**Diagram:**
```
┌─────────────────────────────────────────┐
│           Invoice.Builder               │
├─────────────────────────────────────────┤
│ - invoiceNumber: String                 │
│ - customerName: String                  │
│ - customerEmail: String                 │
│ - items: List<InvoiceItem>              │
│ - taxRate: BigDecimal                   │
│ - discountAmount: BigDecimal            │
├─────────────────────────────────────────┤
│ + invoiceNumber(String): Builder        │
│ + customerName(String): Builder         │
│ + items(List): Builder                  │
│ + taxRate(BigDecimal): Builder          │
│ + build(): Invoice                      │
│ - validate(): void                      │
│ - calculateAmounts(): void              │
└─────────────────────────────────────────┘
           │
           │ creates
           ↓
┌─────────────────────────────────────────┐
│            Invoice                      │
├─────────────────────────────────────────┤
│ - final invoiceNumber: String           │
│ - final customerName: String            │
│ - final items: List<InvoiceItem>        │
│ - final subtotal: BigDecimal            │
│ - final taxAmount: BigDecimal           │
│ - final totalAmount: BigDecimal         │
├─────────────────────────────────────────┤
│ + getInvoiceNumber(): String            │
│ + getCustomerName(): String             │
│ + getTotalAmount(): BigDecimal          │
└─────────────────────────────────────────┘
```

### 2. Order Builder (Manual - Step-by-Step Construction)

**Location:** `com.designpatterns.showcase.builder.manual.OrderBuilder`

**Features:**
- Dynamic item addition
- Running total calculation
- Clear items functionality
- Separate builder and domain object

**Example:**
```java
OrderDetails order = OrderBuilder.newOrder()
    .forUser(userId)
    .addItem("Laptop", 1, new BigDecimal("999.99"))
    .addItem("Mouse", 2, new BigDecimal("25.00"))
    .shippingTo("123 Main Street")
    .payingWith("Credit Card")
    .withStatus(OrderStatus.CONFIRMED)
    .build();
```

**Diagram:**
```
┌─────────────────────────────────────────┐
│          OrderBuilder                   │
├─────────────────────────────────────────┤
│ - userId: Long                          │
│ - items: List<OrderItemDetails>         │
│ - status: OrderStatus                   │
│ - shippingAddress: String               │
├─────────────────────────────────────────┤
│ + forUser(Long): OrderBuilder           │
│ + addItem(String, int, BD): Builder     │
│ + shippingTo(String): OrderBuilder      │
│ + build(): OrderDetails                 │
│ + getCurrentTotal(): BigDecimal         │
│ + clear(): OrderBuilder                 │
└─────────────────────────────────────────┘
           │
           │ creates
           ↓
┌─────────────────────────────────────────┐
│         OrderDetails                    │
├─────────────────────────────────────────┤
│ - final userId: Long                    │
│ - final items: List<OrderItemDetails>   │
│ - final totalAmount: BigDecimal         │
│ - final status: OrderStatus             │
└─────────────────────────────────────────┘
```

### 3. User Registration (Lombok @Builder - Optional Fields)

**Location:** `com.designpatterns.showcase.builder.lombok.UserRegistration`

**Features:**
- Mix of required and optional fields
- Default values for boolean flags
- Optional fields wrapped in `Optional<T>`
- Post-build validation

**Example:**
```java
// Basic registration
UserRegistration basic = UserRegistration.builder()
    .username("johndoe")
    .email("john@example.com")
    .password("secure123")
    .firstName("John")
    .lastName("Doe")
    .termsAccepted(true)
    .build();
basic.validate();

// Corporate registration
UserRegistration corporate = UserRegistration.builder()
    .username("bobsmith")
    .email("bob@company.com")
    .password("secure123")
    .firstName("Bob")
    .lastName("Smith")
    .companyName("Acme Corp")
    .jobTitle("Software Engineer")
    .termsAccepted(true)
    .build();
corporate.validate();
```

### 4. Query Builder (Fluent API - Dynamic Queries)

**Location:** `com.designpatterns.showcase.builder.query.QueryBuilder`

**Features:**
- SQL query construction
- Dynamic where clauses
- Joins, grouping, ordering
- Parameter binding

**Example:**
```java
Query query = QueryBuilder.select("orders.id", "users.name", "orders.total")
    .from("orders")
    .innerJoin("users", "orders.user_id = users.id")
    .where("orders.status", "=", "COMPLETED")
    .whereBetween("orders.total", 100, 1000)
    .orderBy("orders.created_at", "DESC")
    .limit(20)
    .build();

String sql = query.getSql();
Map<String, Object> params = query.getParameters();
```

**Diagram:**
```
┌─────────────────────────────────────────┐
│         QueryBuilder                    │
├─────────────────────────────────────────┤
│ - table: String                         │
│ - selectColumns: List<String>           │
│ - whereConditions: List<WhereCondition> │
│ - joins: List<String>                   │
│ - orderByColumns: List<String>          │
├─────────────────────────────────────────┤
│ + select(String...): QueryBuilder       │
│ + from(String): QueryBuilder            │
│ + where(String, String, Object): QB     │
│ + innerJoin(String, String): QB         │
│ + orderBy(String, String): QB           │
│ + limit(int): QueryBuilder              │
│ + build(): Query                        │
└─────────────────────────────────────────┘
           │
           │ creates
           ↓
┌─────────────────────────────────────────┐
│            Query                        │
├─────────────────────────────────────────┤
│ - final sql: String                     │
│ - final parameters: Map<String, Object> │
└─────────────────────────────────────────┘
```

---

## Manual vs Lombok Builder

### Comparison Table

| Feature | Manual Builder | Lombok @Builder |
|---------|----------------|-----------------|
| **Boilerplate** | High | Low |
| **Custom Validation** | In `build()` method | Separate validation method |
| **Calculations** | Yes, in `build()` | No, must be done separately |
| **Custom Methods** | Yes (e.g., `addItem()`) | Limited |
| **Default Values** | Constructor initialization | `@Builder.Default` |
| **Immutability** | Guaranteed | Depends on field modifiers |
| **Code Maintainability** | More code, more tests | Less code, less maintenance |
| **Flexibility** | Maximum | Limited to Lombok features |
| **IDE Support** | Native | Plugin required |

### When to Choose Manual Builder

1. **Complex validation logic** that depends on multiple fields
2. **Calculated fields** that need to be computed during build
3. **Custom builder methods** (e.g., `addItem()`, `clear()`)
4. **Step-by-step construction** with intermediate state queries
5. **Full control** over the build process
6. **Builder reusability** (e.g., clearing and reusing)

**Example: Invoice requires complex calculations**
```java
// In Invoice.Builder
private void calculateAmounts() {
    this.subtotal = items.stream()
        .map(InvoiceItem::getTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    BigDecimal afterDiscount = subtotal.subtract(discountAmount);
    this.taxAmount = afterDiscount.multiply(taxRate);
    this.totalAmount = afterDiscount.add(taxAmount);
}
```

### When to Choose Lombok @Builder

1. **Simple POJOs** with many fields
2. **Mostly optional fields**
3. **No complex calculations** during construction
4. **Rapid development** preferred
5. **Minimal boilerplate** desired
6. **Immutable DTOs** or value objects

**Example: UserRegistration is a simple data holder**
```java
@Getter
@Builder
public class UserRegistration {
    private final String username;
    private final String email;
    @Builder.Default
    private final boolean termsAccepted = false;
}
```

---

## Common Pitfalls

### 1. **Mutable Builders Leading to Thread Safety Issues**

❌ **Problem:**
```java
// Shared mutable builder
private static final OrderBuilder sharedBuilder = OrderBuilder.newOrder();

// Thread 1
sharedBuilder.forUser(1L).addItem("Item A", 1, price1).build();

// Thread 2 (corrupts Thread 1's data!)
sharedBuilder.forUser(2L).addItem("Item B", 1, price2).build();
```

✅ **Solution:** Always create new builder instances
```java
// Each thread gets its own builder
OrderBuilder builder1 = OrderBuilder.newOrder();
OrderBuilder builder2 = OrderBuilder.newOrder();
```

### 2. **Not Validating After Lombok Builder**

❌ **Problem:**
```java
UserRegistration registration = UserRegistration.builder()
    .username("test")
    .email("invalid-email")  // Invalid!
    .build();
// No validation occurred!
```

✅ **Solution:** Always call validate()
```java
UserRegistration registration = UserRegistration.builder()
    .username("test")
    .email("test@example.com")
    .termsAccepted(true)
    .build();
registration.validate();  // Throws exception if invalid
```

### 3. **Forgetting to Make Objects Immutable**

❌ **Problem:**
```java
public class Invoice {
    private List<InvoiceItem> items;  // Mutable!
    
    public List<InvoiceItem> getItems() {
        return items;  // Exposes internal state!
    }
}

Invoice invoice = invoice.build();
invoice.getItems().clear();  // Mutates the invoice!
```

✅ **Solution:** Use final fields and defensive copies
```java
public final class Invoice {
    private final List<InvoiceItem> items;
    
    private Invoice(Builder builder) {
        this.items = Collections.unmodifiableList(
            new ArrayList<>(builder.items)
        );
    }
    
    public List<InvoiceItem> getItems() {
        return items;  // Already unmodifiable
    }
}
```

### 4. **Not Providing Defaults for Optional Fields**

❌ **Problem:**
```java
Invoice invoice = Invoice.builder()
    .invoiceNumber("INV-001")
    .customerName("Test")
    .customerEmail("test@example.com")
    .items(items)
    .build();
// taxRate is null! Causes NullPointerException in calculations
```

✅ **Solution:** Initialize defaults in constructor
```java
private Builder() {
    this.taxRate = BigDecimal.ZERO;
    this.discountAmount = BigDecimal.ZERO;
    this.issueDate = LocalDate.now();
}
```

### 5. **Over-Engineering Simple Objects**

❌ **Problem:**
```java
// Simple 2-field class with builder
public class Point {
    private final int x;
    private final int y;
    
    private Point(Builder builder) { ... }
    
    public static class Builder {
        // 20+ lines of builder code
    }
}
```

✅ **Solution:** Use constructor for simple objects
```java
public class Point {
    private final int x;
    private final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
```

### 6. **Inconsistent Validation**

❌ **Problem:**
```java
// Validation happens in multiple places
builder.addItem("", 1, price);  // No validation
builder.build();  // Partial validation
```

✅ **Solution:** Centralize validation
```java
public Builder addItem(String name, int qty, BigDecimal price) {
    if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException("Name required");
    }
    // ... add item
    return this;
}

public Invoice build() {
    validate();  // Final validation
    return new Invoice(this);
}
```

---

## Best Practices

### 1. **Method Naming Conventions**
```java
// Static factory method for builder
public static Builder builder() { }

// Method names match field names
public Builder customerName(String customerName) { }

// Boolean fields can use "with" prefix
public Builder withMarketingOptIn(boolean marketingOptIn) { }

// Collections can use "add" methods
public Builder addItem(OrderItem item) { }
```

### 2. **Return Type for Chaining**
```java
// Always return 'this' for chaining
public Builder customerName(String customerName) {
    this.customerName = customerName;
    return this;  // Enable chaining
}
```

### 3. **Validation Strategy**
```java
// Early validation for critical issues
public Builder addItem(String name, int qty, BigDecimal price) {
    if (qty <= 0) {
        throw new IllegalArgumentException("Quantity must be positive");
    }
    // ... add item
}

// Comprehensive validation at build time
public Invoice build() {
    validate();  // Check all constraints
    calculateAmounts();
    return new Invoice(this);
}
```

### 4. **Immutability**
```java
// Make the built object immutable
public final class Invoice {
    private final String invoiceNumber;
    private final List<InvoiceItem> items;
    
    private Invoice(Builder builder) {
        this.invoiceNumber = builder.invoiceNumber;
        // Defensive copy + unmodifiable wrapper
        this.items = Collections.unmodifiableList(
            new ArrayList<>(builder.items)
        );
    }
}
```

### 5. **Default Values**
```java
// Set sensible defaults in builder constructor
private Builder() {
    this.issueDate = LocalDate.now();
    this.dueDate = LocalDate.now().plusDays(30);
    this.taxRate = BigDecimal.ZERO;
    this.paymentTerms = "Net 30";
}
```

### 6. **Documentation**
```java
/**
 * Builder for constructing Invoice objects.
 * 
 * <p>Required fields:
 * <ul>
 *   <li>invoiceNumber</li>
 *   <li>customerName</li>
 *   <li>customerEmail</li>
 *   <li>items (at least one)</li>
 * </ul>
 * 
 * <p>Example:
 * <pre>
 * Invoice invoice = Invoice.builder()
 *     .invoiceNumber("INV-001")
 *     .customerName("John Doe")
 *     .customerEmail("john@example.com")
 *     .items(items)
 *     .build();
 * </pre>
 */
public static final class Builder {
    // ...
}
```

### 7. **Testing**
```java
@Test
void shouldValidateRequiredFields() {
    Exception exception = assertThrows(IllegalStateException.class, () -> {
        Invoice.builder()
            .customerName("Test")
            // Missing required fields
            .build();
    });
    assertEquals("Invoice number is required", exception.getMessage());
}

@Test
void shouldEnsureImmutability() {
    List<InvoiceItem> items = new ArrayList<>();
    items.add(item);
    
    Invoice invoice = Invoice.builder()
        .invoiceNumber("INV-001")
        .customerName("Test")
        .customerEmail("test@example.com")
        .items(items)
        .build();
    
    // Modifying original list shouldn't affect invoice
    items.clear();
    assertEquals(1, invoice.getItems().size());
    
    // Invoice's list should be unmodifiable
    assertThrows(UnsupportedOperationException.class, () -> {
        invoice.getItems().clear();
    });
}
```

---

## Summary

The Builder pattern is a powerful tool for creating complex objects with:
- **Improved readability** through fluent interfaces
- **Flexible construction** with optional parameters
- **Built-in validation** ensuring object consistency
- **Immutability** for thread-safety

Choose **manual builders** for complex domain objects requiring custom logic, and **Lombok @Builder** for simple POJOs. Always validate inputs, ensure immutability, and follow consistent naming conventions.

For more examples and tests, see:
- `src/test/java/com/designpatterns/showcase/builder/`

---

## References

- **Design Patterns: Elements of Reusable Object-Oriented Software** by Gang of Four
- **Effective Java (3rd Edition)** by Joshua Bloch - Item 2: "Consider a builder when faced with many constructor parameters"
- **Project Lombok Documentation**: https://projectlombok.org/features/Builder
