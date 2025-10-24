# Strategy Pattern Implementation

This package demonstrates the **Strategy Pattern**, a behavioral design pattern that defines a family of algorithms, encapsulates each one, and makes them interchangeable. The Strategy pattern lets the algorithm vary independently from clients that use it.

## Table of Contents

- [Overview](#overview)
- [Motivation](#motivation)
- [Implementation Details](#implementation-details)
- [Strategy Selection](#strategy-selection)
- [Extension Guide](#extension-guide)
- [Common Pitfalls](#common-pitfalls)
- [Decision Tables](#decision-tables)
- [Usage Examples](#usage-examples)
- [Testing](#testing)
- [API Endpoints](#api-endpoints)

## Overview

This implementation showcases three main strategy families:

1. **DiscountStrategy**: Calculates discounts based on different rules
   - `PercentageDiscountStrategy`: Tier-based percentage discounts (Bronze 5%, Silver 10%, Gold 15%, Platinum 20%)
   - `FixedDiscountStrategy`: Fixed amount discounts based on order value and item count
   - `SeasonalDiscountStrategy`: Time-based discounts (Holiday 25%, Summer 15%, Spring 10%)

2. **ShippingCostStrategy**: Calculates shipping costs based on delivery method
   - `StandardShippingStrategy`: 5-7 business days domestic shipping
   - `ExpressShippingStrategy`: 1-2 business days expedited shipping
   - `InternationalShippingStrategy`: International shipping with customs fees

3. **PaymentValidationStrategy**: Validates payments using different criteria
   - `BasicPaymentValidationStrategy`: Validates required fields and amount limits
   - `FraudDetectionValidationStrategy`: Assesses fraud risk based on transaction patterns
   - `ComplianceValidationStrategy`: Ensures regulatory compliance (AML, sanctions)

## Motivation

### Why Use the Strategy Pattern?

#### 1. **Eliminate Complex Conditional Logic**

Without the Strategy pattern, code becomes cluttered with nested if-else statements:

```java
// ❌ BAD: Complex conditional logic
public BigDecimal calculateDiscount(Order order) {
    if (order.getCustomerTier().equals("PLATINUM")) {
        if (order.getAmount().compareTo(new BigDecimal("100")) >= 0) {
            return order.getAmount().multiply(new BigDecimal("0.20"));
        }
    } else if (order.getCustomerTier().equals("GOLD")) {
        return order.getAmount().multiply(new BigDecimal("0.15"));
    } else if (order.getDate().getMonth() == Month.DECEMBER) {
        return order.getAmount().multiply(new BigDecimal("0.25"));
    } else if (order.getItemCount() >= 3) {
        return new BigDecimal("10.00");
    }
    return BigDecimal.ZERO;
}
```

With the Strategy pattern, each algorithm is encapsulated:

```java
// ✅ GOOD: Clean, encapsulated strategies
public interface DiscountStrategy {
    DiscountResult calculateDiscount(DiscountRequest request);
}

// Each strategy implements its own logic
public class PercentageDiscountStrategy implements DiscountStrategy {
    public DiscountResult calculateDiscount(DiscountRequest request) {
        // Clean, focused implementation
    }
}
```

#### 2. **Open/Closed Principle**

The Strategy pattern allows adding new algorithms without modifying existing code:

```java
// Adding a new strategy is simple - just implement the interface
public class NewYearDiscountStrategy implements DiscountStrategy {
    public DiscountResult calculateDiscount(DiscountRequest request) {
        // New discount logic
    }
}

// Register it in the context
discountContext.addStrategy("NEW_YEAR", new NewYearDiscountStrategy());
```

#### 3. **Runtime Algorithm Selection**

Strategies can be selected dynamically based on configuration, user input, or business rules:

```java
// Select strategy at runtime based on user preference
String userPreference = request.getDiscountPreference();
DiscountResult result = discountContext.applyDiscount(userPreference, request);

// Or automatically select the best strategy
DiscountResult bestResult = discountContext.applyBestDiscount(request);
```

#### 4. **Testability**

Each strategy can be tested in isolation:

```java
@Test
void shouldApplyPlatinumDiscount() {
    PercentageDiscountStrategy strategy = new PercentageDiscountStrategy();
    DiscountRequest request = DiscountRequest.builder()
        .orderAmount(new BigDecimal("500.00"))
        .customerTier("PLATINUM")
        .build();
    
    DiscountResult result = strategy.calculateDiscount(request);
    
    assertEquals(new BigDecimal("100.00"), result.getDiscountAmount());
}
```

## Implementation Details

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
│                   (Controllers, Services)                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Context Classes                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Discount    │  │   Shipping   │  │   Payment    │          │
│  │   Context    │  │   Context    │  │  Validation  │          │
│  │              │  │              │  │   Context    │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼──────────────────┼──────────────────┼──────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Strategy Interfaces                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Discount    │  │ Shipping     │  │   Payment    │          │
│  │  Strategy    │  │ Cost         │  │  Validation  │          │
│  │              │  │ Strategy     │  │  Strategy    │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼──────────────────┼──────────────────┼──────────────────┘
          │                  │                  │
┌─────────┴──────────┬───────┴──────────┬───────┴────────────┐
│                    │                  │                    │
▼                    ▼                  ▼                    ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Percentage   │  │   Fixed      │  │  Seasonal    │  │   Standard   │
│  Discount    │  │  Discount    │  │  Discount    │  │  Shipping    │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Express    │  │International │  │    Basic     │  │    Fraud     │
│  Shipping    │  │  Shipping    │  │ Validation   │  │  Detection   │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
┌──────────────┐
│ Compliance   │
│ Validation   │
└──────────────┘
```

### 1. Discount Strategies

#### PercentageDiscountStrategy

Applies tier-based percentage discounts to orders meeting minimum thresholds.

**Business Rules:**
- Minimum order amount: $100.00
- Bronze tier: 5% discount
- Silver tier: 10% discount
- Gold tier: 15% discount
- Platinum tier: 20% discount

**Example:**
```java
// Order: $500.00, Customer: GOLD tier
// Discount: $500.00 × 15% = $75.00
// Final: $425.00
```

#### FixedDiscountStrategy

Applies fixed dollar amount discounts based on order value and item count.

**Business Rules:**
- Multi-item discount: $10.00 off for 3+ items and $50+ order
- Bulk order discount: $25.00 off for orders $200+

**Example:**
```java
// Order: $250.00, Items: 5
// Discount: $25.00 (bulk discount)
// Final: $225.00
```

#### SeasonalDiscountStrategy

Applies time-based discounts during specific periods.

**Business Rules:**
- Minimum order amount: $75.00
- Holiday Season (Nov 20 - Dec 31): 25% off
- Summer Sale (Jun - Aug): 15% off
- Spring Sale (Mar - May): 10% off

**Example:**
```java
// Order: $200.00, Date: December 25
// Discount: $200.00 × 25% = $50.00
// Final: $150.00
```

### 2. Shipping Cost Strategies

#### StandardShippingStrategy

Domestic shipping with 5-7 business day delivery.

**Pricing:**
- Base cost: $5.00
- Weight: $2.00/kg
- Fragile handling: +$3.00
- Signature required: +$2.00
- Supported: USA only

#### ExpressShippingStrategy

Expedited domestic shipping with 1-2 business day delivery.

**Pricing:**
- Base cost: $15.00
- Priority handling: +$10.00
- Weight: $5.00/kg
- Fragile handling: +$5.00
- Signature required: +$3.00
- Supported: USA only

#### InternationalShippingStrategy

International shipping with customs processing.

**Pricing:**
- Base cost: $25.00
- Customs fee: $15.00
- Weight: $8.00/kg
- Fragile handling: +$10.00
- Signature required: +$5.00
- Delivery: 7-15 business days (country-dependent)

### 3. Payment Validation Strategies

#### BasicPaymentValidationStrategy

Validates fundamental payment requirements.

**Checks:**
- Amount range: $0.01 - $10,000.00
- Customer ID presence
- Payment method specified
- Payment details provided

#### FraudDetectionValidationStrategy

Assesses transaction fraud risk.

**Risk Scoring:**
- High transaction amount (>$1,000): +25 points
- Very high amount (>$5,000): +50 points
- High-risk country: +30 points
- Suspicious IP address: +20 points
- Missing IP address: +10 points
- Suspicious card pattern: +40 points
- Risk score ≥70: Transaction blocked

#### ComplianceValidationStrategy

Ensures regulatory compliance.

**Checks:**
- Sanctioned country blocking
- Restricted payment methods
- AML threshold ($10,000): Requires customer identification
- Reporting threshold ($3,000): Triggers regulatory reporting
- Tax ID recommendations for large transactions

## Strategy Selection

### Manual Selection

Explicitly specify which strategy to use:

```java
// Discount
DiscountResult result = discountContext.applyDiscount("PERCENTAGE", request);

// Shipping
ShippingCostResult shipping = shippingContext.calculateShippingCost("EXPRESS", request);

// Payment validation
PaymentValidationResult validation = validationContext.validatePayment("FRAUD_DETECTION", request);
```

### Automatic Selection

Let the context choose the best strategy:

```java
// Automatic best discount selection
DiscountResult bestDiscount = discountContext.applyBestDiscount(request);

// Automatic shipping based on destination
ShippingCostResult shipping = shippingContext.calculateShippingCostAuto(request);

// Validate with all strategies
PaymentValidationResult composite = validationContext.validatePaymentWithAllStrategies(request);
```

### Configuration-Based Selection

Strategies can be selected via configuration files or database settings:

```java
// Load strategy from configuration
String strategy = configService.getDiscountStrategy(customerId);
DiscountResult result = discountContext.applyDiscount(strategy, request);
```

### Rule-Based Selection

Use business rules to determine strategy:

```java
public String selectDiscountStrategy(DiscountRequest request) {
    if (isHolidaySeason(request.getOrderDate())) {
        return "SEASONAL";
    } else if (request.getCustomerTier() != null) {
        return "PERCENTAGE";
    } else if (request.getItemCount() >= 3) {
        return "FIXED";
    }
    return "PERCENTAGE";
}
```

## Extension Guide

### Adding a New Discount Strategy

1. **Implement the interface:**

```java
@Component("studentDiscount")
public class StudentDiscountStrategy implements DiscountStrategy {
    
    @Override
    public DiscountResult calculateDiscount(DiscountRequest request) {
        // Implementation
    }
    
    @Override
    public boolean isApplicable(DiscountRequest request) {
        // Check if student ID is provided
        return request.getStudentId() != null;
    }
    
    @Override
    public String getStrategyName() {
        return "STUDENT";
    }
}
```

2. **Register in context:**

```java
@Component
public class DiscountContext {
    private final Map<String, DiscountStrategy> strategies;
    
    public DiscountContext(
            // ... existing strategies
            @Qualifier("studentDiscount") DiscountStrategy studentDiscount) {
        this.strategies = Map.of(
            // ... existing mappings
            "STUDENT", studentDiscount
        );
    }
}
```

3. **Add tests:**

```java
@Test
void shouldApplyStudentDiscount() {
    StudentDiscountStrategy strategy = new StudentDiscountStrategy();
    DiscountRequest request = DiscountRequest.builder()
        .studentId("STU12345")
        .orderAmount(new BigDecimal("100.00"))
        .build();
    
    DiscountResult result = strategy.calculateDiscount(request);
    
    assertTrue(result.isApplied());
    assertEquals("STUDENT", result.getDiscountType());
}
```

### Adding a New Shipping Strategy

Follow the same pattern with `ShippingCostStrategy`:

```java
@Component("sameDay Shipping")
public class SameDayShippingStrategy implements ShippingCostStrategy {
    // Implementation
}
```

### Adding a New Validation Strategy

Implement `PaymentValidationStrategy`:

```java
@Component("addressValidation")
public class AddressValidationStrategy implements PaymentValidationStrategy {
    // Implementation
}
```

## Common Pitfalls

### ❌ Pitfall 1: Strategy Explosion

**Problem:** Creating too many fine-grained strategies leads to maintenance overhead.

```java
// ❌ BAD: Too many specific strategies
- MondayDiscountStrategy
- TuesdayDiscountStrategy
- WednesdayDiscountStrategy
- GoldCustomerMondayDiscountStrategy
- SilverCustomerTuesdayDiscountStrategy
// ... hundreds of combinations
```

**Solution:** Use composition or parameterized strategies:

```java
// ✅ GOOD: Parameterized strategy
public class DayOfWeekDiscountStrategy implements DiscountStrategy {
    private final Map<DayOfWeek, BigDecimal> discountRates;
    
    public DayOfWeekDiscountStrategy(Map<DayOfWeek, BigDecimal> rates) {
        this.discountRates = rates;
    }
}

// Or combine multiple criteria in one strategy
public class SeasonalDiscountStrategy implements DiscountStrategy {
    // Handles all seasonal variations
}
```

**Guidelines:**
- Group related algorithms into a single strategy
- Use configuration/parameters instead of new classes
- Maximum 5-7 strategies per family
- Consider Chain of Responsibility for multiple rules

### ❌ Pitfall 2: Tight Coupling Between Strategies

**Problem:** Strategies depend on each other's results.

```java
// ❌ BAD: Strategy depends on another strategy
public class CombinedDiscountStrategy implements DiscountStrategy {
    private PercentageDiscountStrategy percentageStrategy;
    
    public DiscountResult calculate(DiscountRequest request) {
        // Tight coupling to specific implementation
        DiscountResult first = percentageStrategy.calculate(request);
        return applyAdditionalDiscount(first);
    }
}
```

**Solution:** Use Composite pattern or Chain of Responsibility:

```java
// ✅ GOOD: Orchestrate at context level
public DiscountResult applyMultipleDiscounts(DiscountRequest request) {
    DiscountResult result1 = percentageStrategy.calculate(request);
    DiscountRequest adjusted = adjustRequest(result1);
    DiscountResult result2 = fixedStrategy.calculate(adjusted);
    return combine(result1, result2);
}
```

### ❌ Pitfall 3: Forgetting to Validate Applicability

**Problem:** Strategies applied without checking if they're appropriate.

```java
// ❌ BAD: Always apply strategy regardless of applicability
public DiscountResult calculate(DiscountRequest request) {
    return applyDiscount(request); // May produce incorrect results
}
```

**Solution:** Always check applicability first:

```java
// ✅ GOOD: Check before applying
@Override
public DiscountResult calculateDiscount(DiscountRequest request) {
    if (!isApplicable(request)) {
        return buildNoDiscountResult(request);
    }
    return applyDiscount(request);
}

@Override
public boolean isApplicable(DiscountRequest request) {
    return request.getOrderAmount() != null 
        && request.getOrderAmount().compareTo(MIN_AMOUNT) >= 0;
}
```

### ❌ Pitfall 4: Bloated Strategy Interface

**Problem:** Interface has too many methods that not all strategies need.

```java
// ❌ BAD: Interface Segregation Principle violation
public interface DiscountStrategy {
    DiscountResult calculate(DiscountRequest request);
    boolean isApplicable(DiscountRequest request);
    List<String> getRequiredFields();
    void validateConfiguration();
    String getDescription();
    BigDecimal getMaxDiscount();
    BigDecimal getMinOrderAmount();
    // ... 10 more methods
}
```

**Solution:** Keep interfaces focused, use composition:

```java
// ✅ GOOD: Focused interface
public interface DiscountStrategy {
    DiscountResult calculateDiscount(DiscountRequest request);
    boolean isApplicable(DiscountRequest request);
    String getStrategyName();
}

// Optional metadata through separate interface
public interface DiscountMetadata {
    String getDescription();
    BigDecimal getMaxDiscount();
}
```

### ❌ Pitfall 5: Not Handling Strategy Selection Errors

**Problem:** No handling for unknown or unavailable strategies.

```java
// ❌ BAD: Null pointer exception waiting to happen
DiscountStrategy strategy = strategies.get(strategyName);
return strategy.calculate(request); // NPE if strategy not found
```

**Solution:** Explicit error handling:

```java
// ✅ GOOD: Explicit exception
public DiscountResult applyDiscount(String strategyType, DiscountRequest request) {
    DiscountStrategy strategy = strategies.get(strategyType.toUpperCase());
    if (strategy == null) {
        throw new UnsupportedStrategyException("Unknown discount strategy: " + strategyType);
    }
    return strategy.calculateDiscount(request);
}
```

### ❌ Pitfall 6: Missing State Management

**Problem:** Strategies maintain state between invocations.

```java
// ❌ BAD: Stateful strategy (not thread-safe)
@Component
public class CountingDiscountStrategy implements DiscountStrategy {
    private int usageCount = 0; // Shared state!
    
    public DiscountResult calculate(DiscountRequest request) {
        usageCount++; // Not thread-safe
        return calculateDiscount(request);
    }
}
```

**Solution:** Keep strategies stateless:

```java
// ✅ GOOD: Stateless strategy
@Component
public class DiscountStrategy implements DiscountStrategy {
    // Only final configuration
    private static final BigDecimal RATE = new BigDecimal("0.15");
    
    public DiscountResult calculate(DiscountRequest request) {
        // No shared mutable state
        return calculateDiscount(request);
    }
}
```

## Decision Tables

### Discount Strategy Selection Matrix

| Condition | Percentage | Fixed | Seasonal | Priority |
|-----------|-----------|-------|----------|----------|
| Customer has tier (Bronze/Silver/Gold/Platinum) | ✓ | | | Medium |
| Order ≥ $100 | ✓ | | | Required |
| Order ≥ $50 AND Items ≥ 3 | | ✓ | | Medium |
| Order ≥ $200 | | ✓ | | High |
| Date in Holiday Season (Nov 20-Dec 31) | | | ✓ | High |
| Date in Summer (Jun-Aug) | | | ✓ | Medium |
| Date in Spring (Mar-May) | | | ✓ | Low |
| Order ≥ $75 | | | ✓ | Required |

**Selection Logic:**
1. Check all applicable strategies
2. Apply the strategy with highest discount amount
3. If no strategy applicable, return no discount

### Shipping Strategy Selection Matrix

| Destination | Standard | Express | International |
|-------------|----------|---------|---------------|
| USA | ✓ | ✓ | |
| US | ✓ | ✓ | |
| UNITED STATES | ✓ | ✓ | |
| CANADA | | | ✓ |
| MEXICO | | | ✓ |
| UK | | | ✓ |
| GERMANY | | | ✓ |
| Other countries | | | ✓ |

**Auto-selection Logic:**
1. Check if destination is USA → Select Standard (cheapest)
2. Otherwise → Select International
3. User can override with Express for faster delivery

### Payment Validation Risk Matrix

| Validation Check | Points | Threshold | Action |
|------------------|--------|-----------|--------|
| Amount > $5,000 | +50 | - | Warn |
| Amount > $1,000 | +25 | - | Warn |
| High-risk country | +30 | - | Warn |
| Suspicious IP | +20 | - | Warn |
| Missing IP | +10 | - | Warn |
| Suspicious card pattern | +40 | - | Error |
| **Total Risk Score** | - | ≥70 | Block |
| Amount ≥ $10,000 | - | AML | Require ID |
| Amount ≥ $3,000 | - | Reporting | Flag |
| Sanctioned country | - | - | Block |
| Restricted payment method | - | - | Block |

## Usage Examples

### Example 1: Calculate Order Discount

```java
@Autowired
private DiscountContext discountContext;

// Apply specific discount strategy
DiscountRequest request = DiscountRequest.builder()
    .customerId("CUST001")
    .orderAmount(new BigDecimal("200.00"))
    .customerTier("GOLD")
    .orderDate(LocalDate.now())
    .build();

DiscountResult result = discountContext.applyDiscount("PERCENTAGE", request);

// Or find best discount automatically
DiscountResult bestResult = discountContext.applyBestDiscount(request);
```

### Example 2: Calculate Shipping Cost

```java
@Autowired
private ShippingContext shippingContext;

// Calculate with specific strategy
ShippingRequest request = ShippingRequest.builder()
    .destinationCountry("USA")
    .packageWeight(new BigDecimal("2.5"))
    .isFragile(true)
    .requiresSignature(false)
    .build();

ShippingCostResult result = shippingContext.calculateShippingCost("EXPRESS", request);

// Or auto-select based on destination
ShippingCostResult autoResult = shippingContext.calculateShippingCostAuto(request);
```

### Example 3: Validate Payment

```java
@Autowired
private PaymentValidationContext validationContext;

Map<String, String> details = new HashMap<>();
details.put("cardNumber", "4111111111111111");

PaymentValidationRequest request = PaymentValidationRequest.builder()
    .customerId("CUST001")
    .amount(new BigDecimal("500.00"))
    .paymentMethod("CREDIT_CARD")
    .ipAddress("203.0.113.1")
    .billingCountry("USA")
    .paymentDetails(details)
    .build();

// Validate with specific strategy
PaymentValidationResult result = validationContext.validatePayment("FRAUD_DETECTION", request);

// Or validate with all strategies
PaymentValidationResult comprehensive = validationContext.validatePaymentWithAllStrategies(request);
```

### Example 4: Complete Pricing Calculation

```java
@Autowired
private PricingService pricingService;

PricingRequest request = PricingRequest.builder()
    .customerId("CUST001")
    .orderAmount(new BigDecimal("300.00"))
    .customerTier("PLATINUM")
    .orderDate(LocalDate.of(2024, 12, 25))
    .itemCount(5)
    .destinationCountry("USA")
    .packageWeight(new BigDecimal("3.0"))
    .isFragile(true)
    .requiresSignature(true)
    .discountStrategy("SEASONAL") // Optional: specify strategy
    .shippingStrategy("EXPRESS")   // Optional: specify strategy
    .build();

PricingResponse response = pricingService.calculateTotalPrice(request);

System.out.println("Subtotal: $" + response.getSubtotal());
System.out.println("Discount: $" + response.getDiscountAmount());
System.out.println("Shipping: $" + response.getShippingCost());
System.out.println("Total: $" + response.getTotalAmount());
System.out.println("Delivery: " + response.getEstimatedDeliveryDays() + " days");
```

## Testing

Comprehensive unit tests are provided for all strategies and contexts:

### Discount Strategy Tests
- `PercentageDiscountStrategyTest`: Tests tier-based discounts
- `FixedDiscountStrategyTest`: Tests fixed amount discounts
- `SeasonalDiscountStrategyTest`: Tests seasonal discounts

### Shipping Strategy Tests
- `StandardShippingStrategyTest`: Tests domestic standard shipping
- `ExpressShippingStrategyTest`: Tests expedited shipping
- `InternationalShippingStrategyTest`: Tests international shipping

### Payment Validation Tests
- `BasicPaymentValidationStrategyTest`: Tests basic validation rules
- `FraudDetectionValidationStrategyTest`: Tests fraud risk assessment
- `ComplianceValidationStrategyTest`: Tests regulatory compliance

### Context Tests
- `DiscountContextTest`: Tests strategy selection and best discount logic
- `ShippingContextTest`: Tests shipping method selection
- `PaymentValidationContextTest`: Tests validation orchestration
- `PricingServiceTest`: Tests end-to-end pricing calculations

### Running Tests

```bash
# Run all strategy tests
mvn test -Dtest="com.designpatterns.showcase.strategy.**.*Test"

# Run specific test class
mvn test -Dtest=PercentageDiscountStrategyTest

# Run with coverage
mvn clean test jacoco:report
```

## API Endpoints

### Discount Endpoints

```bash
# Apply specific discount strategy
POST /api/strategy/discount/{strategy}
Content-Type: application/json

{
  "customerId": "CUST001",
  "orderAmount": 200.00,
  "customerTier": "GOLD",
  "orderDate": "2024-12-25",
  "itemCount": 5
}

# Find best discount automatically
POST /api/strategy/discount/best
Content-Type: application/json

{
  "customerId": "CUST001",
  "orderAmount": 200.00,
  "customerTier": "GOLD",
  "orderDate": "2024-12-25",
  "itemCount": 5
}
```

### Shipping Endpoints

```bash
# Calculate shipping with specific strategy
POST /api/strategy/shipping/{strategy}
Content-Type: application/json

{
  "destinationCountry": "USA",
  "packageWeight": 2.5,
  "isFragile": true,
  "requiresSignature": false
}

# Auto-select shipping strategy
POST /api/strategy/shipping/auto
Content-Type: application/json

{
  "destinationCountry": "CANADA",
  "packageWeight": 3.0,
  "isFragile": false,
  "requiresSignature": true
}
```

### Payment Validation Endpoints

```bash
# Validate with specific strategy
POST /api/strategy/payment/validate/{strategy}
Content-Type: application/json

{
  "customerId": "CUST001",
  "amount": 500.00,
  "paymentMethod": "CREDIT_CARD",
  "ipAddress": "203.0.113.1",
  "billingCountry": "USA",
  "paymentDetails": {
    "cardNumber": "4111111111111111"
  }
}

# Validate with all strategies
POST /api/strategy/payment/validate/all
Content-Type: application/json

{
  "customerId": "CUST001",
  "amount": 500.00,
  "paymentMethod": "CREDIT_CARD",
  "ipAddress": "203.0.113.1",
  "billingCountry": "USA",
  "paymentDetails": {
    "cardNumber": "4111111111111111"
  }
}
```

### Pricing Endpoint

```bash
# Calculate complete pricing with discounts and shipping
POST /api/strategy/pricing
Content-Type: application/json

{
  "customerId": "CUST001",
  "orderAmount": 300.00,
  "customerTier": "PLATINUM",
  "orderDate": "2024-12-25",
  "itemCount": 5,
  "destinationCountry": "USA",
  "packageWeight": 3.0,
  "isFragile": true,
  "requiresSignature": true,
  "discountStrategy": "SEASONAL",
  "shippingStrategy": "EXPRESS"
}
```

## Best Practices

### ✅ DO:

1. **Keep strategies stateless** - Strategies should not maintain state between invocations
2. **Validate applicability** - Always check if a strategy is applicable before applying it
3. **Use meaningful names** - Strategy names should clearly indicate their purpose
4. **Document business rules** - Clearly document the rules each strategy implements
5. **Test each strategy independently** - Write comprehensive unit tests for each strategy
6. **Handle errors gracefully** - Provide clear error messages for unsupported strategies
7. **Use context for orchestration** - Let context classes handle strategy selection logic

### ❌ AVOID:

1. **Don't create strategy explosion** - Group related algorithms instead of creating too many strategies
2. **Don't couple strategies** - Strategies should be independent of each other
3. **Don't bloat interfaces** - Keep strategy interfaces focused and minimal
4. **Don't forget thread-safety** - Strategies should be thread-safe (stateless is easiest)
5. **Don't hard-code strategy selection** - Use configuration or rules for flexibility
6. **Don't ignore extensibility** - Design for easy addition of new strategies
7. **Don't mix concerns** - Keep business logic separate from selection logic

## Related Patterns

- **Factory Pattern**: Used to create strategy instances
- **Template Method**: Alternative when algorithm structure is fixed
- **State Pattern**: Similar structure but for object state transitions
- **Chain of Responsibility**: For applying multiple strategies in sequence
- **Composite Pattern**: For combining multiple strategies

## References

- Design Patterns: Elements of Reusable Object-Oriented Software (Gang of Four)
- Head First Design Patterns
- Effective Java by Joshua Bloch
- Spring Framework Documentation

## License

This implementation is part of the Design Pattern Showcase project and is provided for educational purposes.
