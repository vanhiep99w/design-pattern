# Factory Pattern Implementation

This package demonstrates the **Factory Pattern**, a creational design pattern that provides an interface for creating objects without specifying their exact classes. The pattern promotes loose coupling and enhances code maintainability and extensibility.

## Table of Contents

- [Overview](#overview)
- [Motivation](#motivation)
- [Implementation Details](#implementation-details)
- [Usage Examples](#usage-examples)
- [Benefits](#benefits)
- [Common Pitfalls](#common-pitfalls)
- [Testing](#testing)
- [API Endpoints](#api-endpoints)

## Overview

This implementation showcases two factory patterns:

1. **PaymentProcessorFactory**: Creates different payment processors (Credit Card, PayPal, Cryptocurrency)
2. **NotificationFactory**: Creates different notification services (Email, SMS, Push) using @Component-based factory methods

Both factories demonstrate runtime object creation based on client requirements, with realistic validation and business rules.

## Motivation

### Why Use the Factory Pattern?

#### 1. **Decoupling Object Creation from Usage**
Without factories, code becomes tightly coupled to concrete implementations:

```java
// ❌ BAD: Tight coupling to specific implementations
public class PaymentService {
    public void processPayment(String type, PaymentRequest request) {
        if (type.equals("CREDIT_CARD")) {
            CreditCardPaymentProcessor processor = new CreditCardPaymentProcessor();
            processor.processPayment(request);
        } else if (type.equals("PAYPAL")) {
            PayPalPaymentProcessor processor = new PayPalPaymentProcessor();
            processor.processPayment(request);
        }
        // Adding new payment type requires modifying this class
    }
}
```

With factories, creation logic is centralized:

```java
// ✅ GOOD: Loose coupling through factory
public class PaymentService {
    private final PaymentProcessorFactory factory;
    
    public void processPayment(String type, PaymentRequest request) {
        PaymentProcessor processor = factory.getPaymentProcessor(type);
        processor.processPayment(request);
        // Adding new payment type only requires registering a new processor
    }
}
```

#### 2. **Open/Closed Principle**
The factory pattern allows adding new implementations without modifying existing code. New payment processors can be added by simply creating a new class and registering it with Spring.

#### 3. **Single Responsibility**
Object creation logic is separated from business logic, making each class focused on its primary responsibility.

#### 4. **Testability**
Factories make it easier to inject mock implementations for testing.

## Implementation Details

### 1. Payment Processor Factory

#### Architecture

```
┌──────────────────────────────────────────────────────────┐
│              PaymentProcessorFactory                      │
│  (Manages and provides payment processor instances)      │
└────────────────────┬─────────────────────────────────────┘
                     │
         ┌───────────┴───────────┬─────────────────┐
         │                       │                 │
         ▼                       ▼                 ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   CreditCard    │    │     PayPal      │    │      Crypto     │
│    Processor    │    │    Processor    │    │    Processor    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
    Validates:              Validates:              Validates:
    - Card number           - Email format          - Wallet address
    - Luhn check           - Account ID            - Crypto type
    - CVV                  - Amount limits         - Signature
    - Expiry date          - Calculates fees       - Network fees
    - Amount limits
```

#### Key Components

**PaymentProcessor Interface**
```java
public interface PaymentProcessor {
    PaymentResult processPayment(PaymentRequest request);
    boolean validatePaymentDetails(PaymentRequest request);
    String getPaymentType();
}
```

**CreditCardPaymentProcessor**
- Validates card number using Luhn algorithm
- Checks CVV format (3-4 digits)
- Validates expiry date format and checks if card is not expired
- Enforces transaction limits ($50,000 max)
- Validates card holder name

**PayPalPaymentProcessor**
- Validates email format
- Validates PayPal account ID format (13-17 alphanumeric characters)
- Calculates PayPal fees (2.9% + $0.30)
- Enforces minimum ($0.01) and maximum ($100,000) transaction amounts
- Returns net amount after fees

**CryptoPaymentProcessor**
- Supports Bitcoin (BTC) and Ethereum (ETH)
- Validates wallet addresses using cryptocurrency-specific patterns
- Converts USD to crypto amounts
- Calculates network fees (0.0001 BTC or 0.001 ETH)
- Generates blockchain transaction hashes
- Requires transaction signatures
- Enforces minimum transaction amount ($10)

### 2. Notification Factory

#### Architecture

```
┌──────────────────────────────────────────────────────────┐
│              NotificationFactory                          │
│  (@Configuration with @Bean factory methods)             │
└────────────────────┬─────────────────────────────────────┘
                     │
         ┌───────────┴───────────┬─────────────────┐
         │                       │                 │
         ▼                       ▼                 ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Email       │    │       SMS       │    │      Push       │
│  Notification   │    │   Notification  │    │  Notification   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
    Validates:              Validates:              Validates:
    - Email format          - Phone format          - Device token
    - Subject (≤200)        - Message (≤1600)       - Title (≤65)
    - Message (≤10000)      - Segments calc         - Message (≤240)
                                                    - Platform detect
```

#### Key Components

**NotificationService Interface**
```java
public interface NotificationService {
    NotificationResult send(NotificationRequest request);
    boolean validateRecipient(String recipient);
    String getNotificationType();
}
```

**EmailNotificationService**
- Validates email addresses using regex
- Enforces subject length limit (200 characters)
- Enforces message length limit (10,000 characters)
- Validates that subject and message are not empty

**SmsNotificationService**
- Validates phone numbers (E.164 format)
- Calculates SMS segments (160 characters per segment)
- Enforces message length limit (1,600 characters / 10 segments)
- Handles phone number variations (with/without formatting)

**PushNotificationService**
- Validates device tokens (64-character hex or 100-200 character alphanumeric)
- Enforces title length limit (65 characters)
- Enforces message length limit (240 characters)
- Detects platform (iOS/Android) from metadata
- Validates that title and message are not empty

### 3. Order Processing Service

The `OrderProcessingService` demonstrates runtime factory usage by coordinating payment processing and notifications:

```java
@Service
public class OrderProcessingService {
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final NotificationServiceProvider notificationServiceProvider;
    
    public OrderProcessingResult processOrder(OrderProcessingRequest request) {
        // 1. Get appropriate payment processor
        PaymentProcessor processor = paymentProcessorFactory
            .getPaymentProcessor(request.getPaymentType());
        
        // 2. Process payment
        PaymentResult paymentResult = processor.processPayment(paymentRequest);
        
        // 3. Send notification if payment successful
        if (paymentResult.isSuccess()) {
            NotificationService service = notificationServiceProvider
                .getNotificationService(request.getNotificationType());
            NotificationResult notificationResult = service.send(notificationRequest);
        }
        
        return result;
    }
}
```

## Usage Examples

### Example 1: Processing Credit Card Payment

```bash
curl -X POST http://localhost:8080/api/factory-demo/process-order \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "amount": 150.00,
    "currency": "USD",
    "paymentType": "CREDIT_CARD",
    "paymentDetails": {
      "cardNumber": "4532015112830366",
      "cvv": "123",
      "expiryDate": "12/25",
      "cardHolderName": "John Doe"
    },
    "notificationType": "EMAIL",
    "notificationRecipient": "john.doe@example.com"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Order processed successfully",
  "paymentResult": {
    "success": true,
    "transactionId": "CC-a1b2c3d4-e5f6-...",
    "message": "Payment processed successfully via Credit Card",
    "processedAmount": 150.00,
    "paymentType": "CREDIT_CARD",
    "timestamp": "2024-01-15T10:30:00"
  },
  "notificationResult": {
    "success": true,
    "notificationId": "EMAIL-x9y8z7w6-...",
    "message": "Email sent successfully to john.doe@example.com",
    "notificationType": "EMAIL",
    "timestamp": "2024-01-15T10:30:01"
  }
}
```

### Example 2: Processing PayPal Payment with SMS Notification

```bash
curl -X POST http://localhost:8080/api/factory-demo/process-order \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST002",
    "amount": 75.50,
    "currency": "USD",
    "paymentType": "PAYPAL",
    "paymentDetails": {
      "email": "jane.smith@example.com",
      "accountId": "ABCD1234567890XYZ"
    },
    "notificationType": "SMS",
    "notificationRecipient": "+1234567890"
  }'
```

### Example 3: Processing Cryptocurrency Payment

```bash
curl -X POST http://localhost:8080/api/factory-demo/process-order \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST003",
    "amount": 500.00,
    "currency": "USD",
    "paymentType": "CRYPTOCURRENCY",
    "paymentDetails": {
      "walletAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
      "cryptoType": "BTC",
      "signature": "304502210..."
    },
    "notificationType": "PUSH",
    "notificationRecipient": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2",
    "notificationMetadata": {
      "platform": "iOS"
    }
  }'
```

### Example 4: Getting Supported Types

```bash
# Get supported payment types
curl http://localhost:8080/api/factory-demo/supported-payment-types

# Response:
{
  "CREDIT_CARD": true,
  "PAYPAL": true,
  "CRYPTOCURRENCY": true
}

# Get supported notification types
curl http://localhost:8080/api/factory-demo/supported-notification-types

# Response:
{
  "EMAIL": true,
  "SMS": true,
  "PUSH": true
}
```

## Benefits

### 1. **Extensibility**

Adding new payment processors or notification services is straightforward:

```java
// Add a new payment processor
@Component("bankTransferProcessor")
public class BankTransferPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // Implementation
    }
    
    @Override
    public String getPaymentType() {
        return "BANK_TRANSFER";
    }
    
    // Other methods...
}

// That's it! The factory automatically discovers and registers it
```

### 2. **Maintainability**

Each payment processor and notification service is isolated in its own class with single responsibility. Changes to one implementation don't affect others.

### 3. **Testability**

Factories enable easy unit testing with mocks:

```java
@Test
void shouldProcessPayment() {
    PaymentProcessor mockProcessor = mock(PaymentProcessor.class);
    when(factory.getPaymentProcessor("TEST")).thenReturn(mockProcessor);
    
    // Test logic...
}
```

### 4. **Configuration Flexibility**

Spring's dependency injection makes it easy to configure different implementations for different environments (dev, test, prod).

### 5. **Runtime Flexibility**

The factory pattern allows selecting implementations at runtime based on user input, configuration, or business rules.

## Common Pitfalls

### 1. **Factory Bloat**

**Problem**: Factories can become bloated if they accumulate too much logic or handle too many responsibilities.

```java
// ❌ BAD: Factory with too much logic
public class PaymentProcessorFactory {
    public PaymentProcessor getPaymentProcessor(String type) {
        if (type.equals("CREDIT_CARD")) {
            // 50 lines of initialization logic
            // Multiple dependencies
            // Complex configuration
            return processor;
        }
        // More complex logic for each type...
    }
}
```

**Solution**: Keep factories simple and delegate complex initialization to builders or configuration classes:

```java
// ✅ GOOD: Simple factory with dependency injection
@Component
public class PaymentProcessorFactory {
    private final Map<String, PaymentProcessor> processors;
    
    @Autowired
    public PaymentProcessorFactory(List<PaymentProcessor> processors) {
        // Simple registration logic
    }
}
```

### 2. **Overuse of String Constants**

**Problem**: Using raw strings for type identification is error-prone:

```java
// ❌ BAD: Magic strings
processor = factory.getPaymentProcessor("credit_card"); // Typo risk
processor = factory.getPaymentProcessor("CREDIT_CARD"); // Inconsistent
```

**Solution**: Use enums or constants:

```java
// ✅ GOOD: Type-safe enums
public enum PaymentType {
    CREDIT_CARD, PAYPAL, CRYPTOCURRENCY
}

processor = factory.getPaymentProcessor(PaymentType.CREDIT_CARD);
```

### 3. **Not Handling Unsupported Types**

**Problem**: Failing to handle unsupported types gracefully:

```java
// ❌ BAD: Returns null or throws generic exception
public PaymentProcessor getPaymentProcessor(String type) {
    return processors.get(type); // Returns null if not found
}
```

**Solution**: Throw descriptive exceptions:

```java
// ✅ GOOD: Clear error handling
public PaymentProcessor getPaymentProcessor(String type) {
    PaymentProcessor processor = processors.get(type);
    if (processor == null) {
        throw new UnsupportedPaymentTypeException(
            "Unsupported payment type: " + type
        );
    }
    return processor;
}
```

### 4. **Mixing Creation and Business Logic**

**Problem**: Putting business logic in the factory:

```java
// ❌ BAD: Business logic in factory
public PaymentResult processPayment(String type, PaymentRequest request) {
    PaymentProcessor processor = getPaymentProcessor(type);
    // Validation logic
    // Fee calculation
    // Notification sending
    return processor.processPayment(request);
}
```

**Solution**: Keep factories focused on object creation:

```java
// ✅ GOOD: Factory only creates objects
public PaymentProcessor getPaymentProcessor(String type) {
    return processors.get(type);
}

// Business logic in service layer
@Service
public class PaymentService {
    public PaymentResult processPayment(String type, PaymentRequest request) {
        PaymentProcessor processor = factory.getPaymentProcessor(type);
        return processor.processPayment(request);
    }
}
```

### 5. **Not Providing Factory Discovery Mechanisms**

**Problem**: Clients don't know what implementations are available:

```java
// ❌ BAD: No way to discover available types
processor = factory.getPaymentProcessor(userInput); // Might fail
```

**Solution**: Provide methods to discover available implementations:

```java
// ✅ GOOD: Discovery methods
public boolean isPaymentTypeSupported(String type) {
    return processors.containsKey(type);
}

public Map<String, PaymentProcessor> getAllProcessors() {
    return new HashMap<>(processors);
}
```

### 6. **Exposing Mutable Factory State**

**Problem**: Returning internal collections directly:

```java
// ❌ BAD: Exposes internal state
public Map<String, PaymentProcessor> getAllProcessors() {
    return processors; // Caller can modify this!
}
```

**Solution**: Return defensive copies:

```java
// ✅ GOOD: Returns immutable copy
public Map<String, PaymentProcessor> getAllProcessors() {
    return new HashMap<>(processors);
}
```

## Testing

This implementation includes comprehensive unit tests covering:

### Factory Tests
- **PaymentProcessorFactoryTest**: Tests factory selection logic, case handling, and error scenarios
- **NotificationFactoryTest**: Tests notification service provider functionality

### Implementation Tests
- **CreditCardPaymentProcessorTest**: Tests card validation, Luhn check, expiry validation, and limits
- **PayPalPaymentProcessorTest**: Tests email/account validation, fee calculation, and amount limits
- **CryptoPaymentProcessorTest**: Tests wallet validation, crypto conversion, and network fees
- **EmailNotificationServiceTest**: Tests email validation, length limits, and content validation
- **SmsNotificationServiceTest**: Tests phone validation, segment calculation, and message limits
- **PushNotificationServiceTest**: Tests device token validation, platform detection, and length limits

### Integration Tests
- **OrderProcessingServiceTest**: Tests end-to-end order processing with mocked dependencies

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PaymentProcessorFactoryTest

# Run factory package tests
mvn test -Dtest="com.designpatterns.showcase.factory.*"

# Run with coverage
mvn clean test jacoco:report
```

## API Endpoints

### POST /api/factory-demo/process-order
Process an order with payment and optional notification.

**Request Body:**
```json
{
  "customerId": "string",
  "amount": number,
  "currency": "string",
  "paymentType": "CREDIT_CARD|PAYPAL|CRYPTOCURRENCY",
  "paymentDetails": {
    // Payment-specific fields
  },
  "notificationType": "EMAIL|SMS|PUSH",
  "notificationRecipient": "string",
  "notificationMetadata": {}
}
```

### GET /api/factory-demo/supported-payment-types
Get list of supported payment types.

**Response:**
```json
{
  "CREDIT_CARD": true,
  "PAYPAL": true,
  "CRYPTOCURRENCY": true
}
```

### GET /api/factory-demo/supported-notification-types
Get list of supported notification types.

**Response:**
```json
{
  "EMAIL": true,
  "SMS": true,
  "PUSH": true
}
```

## Best Practices Summary

✅ **DO:**
- Use dependency injection to register implementations
- Keep factories simple and focused on object creation
- Provide type discovery mechanisms
- Handle unsupported types with clear exceptions
- Return defensive copies of internal collections
- Write comprehensive unit tests for each implementation
- Use interfaces to define contracts
- Document validation rules and business constraints

❌ **DON'T:**
- Mix business logic with factory logic
- Use magic strings without constants or enums
- Return null for unsupported types
- Expose mutable internal state
- Create complex initialization logic in factories
- Hardcode implementations in factory code
- Forget to validate inputs in implementations
- Skip edge case testing

## Further Reading

- [Factory Method Pattern - Gang of Four](https://en.wikipedia.org/wiki/Factory_method_pattern)
- [Spring Framework Dependency Injection](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Test-Driven Development](https://en.wikipedia.org/wiki/Test-driven_development)
