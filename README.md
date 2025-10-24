# Design Pattern Showcase

A comprehensive Spring Boot 3.x application demonstrating various software design patterns with practical examples.

## Overview

This project showcases common design patterns (Creational, Structural, and Behavioral) using a modern Spring Boot application. It includes a fully configured environment with sample domain models, repositories, and test suites to help you understand how design patterns can be implemented in real-world Java applications.

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Maven**: Build and dependency management
- **H2 Database**: In-memory database for development and testing
- **Spring Data JPA**: Data persistence layer
- **Spring AOP**: Aspect-oriented programming support
- **Lombok**: Reduces boilerplate code
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework for unit tests
- **Jakarta Validation**: Bean validation

## Project Structure

```
src/
├── main/
│   ├── java/com/designpatterns/showcase/
│   │   ├── DesignPatternApplication.java    # Main application class
│   │   ├── common/                           # Shared resources
│   │   │   ├── domain/                       # Domain entities (User, Product, Order)
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   └── repository/                   # JPA repositories
│   │   ├── mvc/                              # MVC Pattern Showcase
│   │   │   ├── controller/                   # REST Controllers
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── OrderController.java
│   │   │   ├── service/                      # Business Logic Layer
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── UserService.java
│   │   │   │   └── OrderService.java
│   │   │   ├── repository/                   # Data Access Layer
│   │   │   │   └── OrderRepository.java
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── UserDTO.java
│   │   │   │   ├── OrderDTO.java
│   │   │   │   └── OrderItemDTO.java
│   │   │   ├── mapper/                       # DTO/Entity Mappers
│   │   │   │   ├── ProductMapper.java
│   │   │   │   ├── UserMapper.java
│   │   │   │   └── OrderMapper.java
│   │   │   ├── exception/                    # Exception Handling
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── InvalidRequestException.java
│   │   │   │   └── ErrorResponse.java
│   │   │   └── DataSeeder.java               # Sample data loader
│   │   ├── dependencyinjection/              # DI Pattern Examples
│   │   ├── creational/                       # Creational patterns
│   │   ├── structural/                       # Structural patterns
│   │   └── behavioral/                       # Behavioral patterns
│   └── resources/
│       ├── application.properties            # Main configuration
│       ├── application-dev.properties        # Development profile
│       └── application-prod.properties       # Production profile
└── test/
    └── java/com/designpatterns/showcase/
        ├── mvc/
        │   ├── controller/                   # Controller Integration Tests
        │   │   ├── ProductControllerTest.java
        │   │   ├── UserControllerTest.java
        │   │   └── OrderControllerTest.java
        │   └── exception/                    # Exception Handler Tests
        │       └── GlobalExceptionHandlerTest.java
        └── ...                               # Other test classes
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

## Getting Started

### Clone the Repository

```bash
git clone <repository-url>
cd showcase
```

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

Or with a specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

### Access H2 Console

When running in development mode, you can access the H2 database console at:

```
http://localhost:8080/h2-console
```

**Connection details:**
- JDBC URL: `jdbc:h2:mem:designpatterndb-dev`
- Username: `sa`
- Password: (leave blank)

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=DesignPatternApplicationTests
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report
```

## Configuration Profiles

The application supports multiple profiles:

- **dev** (default): Development environment with H2 in-memory database and verbose logging
- **prod**: Production environment with file-based H2 database and minimal logging
- **test**: Testing environment used during automated tests

Switch profiles by setting the `spring.profiles.active` property or using environment variables:

```bash
java -jar target/showcase-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## Sample Data

The application automatically loads sample data on startup (except in test profile):

- **Users**: 4 sample users with different roles (ADMIN, USER, GUEST)
- **Products**: 6 sample products across different categories (ELECTRONICS, CLOTHING, BOOKS, FOOD)

## Common Domain Models

### User
- Represents application users with authentication and role management
- Fields: username, email, firstName, lastName, role, active status
- Roles: ADMIN, USER, GUEST

### Product
- Represents products in an e-commerce system
- Fields: name, description, price, category, stockQuantity, availability
- Categories: ELECTRONICS, CLOTHING, FOOD, BOOKS, GENERAL

## MVC Pattern Showcase

The project includes a comprehensive implementation of the Model-View-Controller (MVC) pattern using Spring Boot's REST architecture.

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│                   (HTTP Requests/Responses)                  │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    Controller Layer                          │
│                (@RestController, @RequestMapping)            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Product    │  │     User     │  │    Order     │     │
│  │  Controller  │  │  Controller  │  │  Controller  │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼─────────────┐
│                      Service Layer                           │
│      (Business Logic, Transaction Management)                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Product    │  │     User     │  │    Order     │     │
│  │   Service    │  │   Service    │  │   Service    │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼─────────────┐
│                   Repository Layer                           │
│              (Spring Data JPA Repositories)                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Product    │  │     User     │  │    Order     │     │
│  │  Repository  │  │  Repository  │  │  Repository  │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼─────────────┐
│                    Database Layer                            │
│                    (H2 In-Memory)                            │
└─────────────────────────────────────────────────────────────┘

     ┌─────────────────────────────────────────────────────┐
     │         Cross-Cutting Concerns                       │
     │  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
     │  │  Exception   │  │     DTO      │  │  Mapper   │ │
     │  │   Handler    │  │   Validation │  │  Classes  │ │
     │  └──────────────┘  └──────────────┘  └───────────┘ │
     └─────────────────────────────────────────────────────┘
```

### MVC Components

#### 1. Controllers (Presentation Layer)
Located in `mvc/controller/`, controllers handle HTTP requests and responses:

- **ProductController**: Manages product CRUD operations
  - `GET /api/products` - List all products
  - `GET /api/products/{id}` - Get product by ID
  - `GET /api/products/category/{category}` - Filter by category
  - `POST /api/products` - Create new product
  - `PUT /api/products/{id}` - Update product
  - `DELETE /api/products/{id}` - Delete product

- **UserController**: Manages user operations
  - `GET /api/users` - List all users
  - `GET /api/users/{id}` - Get user by ID
  - `GET /api/users/role/{role}` - Filter by role
  - `POST /api/users` - Create new user
  - `PUT /api/users/{id}` - Update user
  - `DELETE /api/users/{id}` - Delete user

- **OrderController**: Manages order operations
  - `GET /api/orders` - List all orders
  - `GET /api/orders/{id}` - Get order by ID
  - `GET /api/orders/user/{userId}` - Get user's orders
  - `POST /api/orders` - Create new order
  - `PATCH /api/orders/{id}/status` - Update order status
  - `DELETE /api/orders/{id}` - Cancel order

#### 2. Services (Business Logic Layer)
Located in `mvc/service/`, services contain business logic and transaction management:

- Implement business rules and validation
- Coordinate between multiple repositories
- Handle transactional operations
- Throw custom exceptions for error scenarios

#### 3. Repositories (Data Access Layer)
Located in `mvc/repository/`, repositories extend Spring Data JPA:

- Provide CRUD operations
- Define custom query methods
- Handle database interactions

#### 4. DTOs (Data Transfer Objects)
Located in `mvc/dto/`, DTOs transfer data between layers:

- Include validation annotations (`@NotBlank`, `@Email`, etc.)
- Separate API contract from domain model
- Prevent over-fetching or exposing sensitive data

#### 5. Mappers
Located in `mvc/mapper/`, mappers convert between entities and DTOs:

- `ProductMapper` - Entity ↔ DTO conversion for products
- `UserMapper` - Entity ↔ DTO conversion for users
- `OrderMapper` - Entity ↔ DTO conversion for orders

#### 6. Exception Handling
Located in `mvc/exception/`, centralized error handling:

- **GlobalExceptionHandler** (`@ControllerAdvice`)
  - Handles `ResourceNotFoundException` (404)
  - Handles `InvalidRequestException` (400)
  - Handles validation errors (`MethodArgumentNotValidException`)
  - Handles generic exceptions (500)

- **ErrorResponse** - Standardized error response model with:
  - Timestamp
  - HTTP status code
  - Error type
  - Error message
  - Request path
  - Validation errors (if applicable)

### Best Practices Demonstrated

#### ✅ DO:
1. **Thin Controllers**: Controllers only handle HTTP concerns, delegate business logic to services
2. **Service Layer**: Encapsulate business logic and orchestration in services
3. **DTO Pattern**: Use DTOs to decouple API contracts from domain models
4. **Validation**: Validate input at the DTO level using Bean Validation
5. **Exception Handling**: Use `@ControllerAdvice` for centralized error handling
6. **Transactional Services**: Mark service methods with `@Transactional`
7. **Logging**: Log at appropriate levels (DEBUG for inputs, INFO for operations)
8. **HTTP Status Codes**: Return proper status codes (200 OK, 201 Created, 204 No Content, 404 Not Found)

#### ❌ AVOID:
1. **Fat Controllers**: Don't put business logic in controllers
   ```java
   // ❌ BAD - Business logic in controller
   @PostMapping
   public ResponseEntity<Product> createProduct(@RequestBody Product product) {
       if (product.getStockQuantity() < 0) {
           throw new IllegalArgumentException("Stock cannot be negative");
       }
       product.setAvailable(product.getStockQuantity() > 0);
       return ResponseEntity.ok(productRepository.save(product));
   }
   
   // ✅ GOOD - Delegate to service
   @PostMapping
   public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO dto) {
       ProductDTO created = productService.createProduct(dto);
       return ResponseEntity.status(HttpStatus.CREATED).body(created);
   }
   ```

2. **Exposing Entities**: Don't return JPA entities directly from controllers
   ```java
   // ❌ BAD - Exposing entity
   @GetMapping("/{id}")
   public ResponseEntity<User> getUser(@PathVariable Long id) {
       return ResponseEntity.ok(userRepository.findById(id).orElseThrow());
   }
   
   // ✅ GOOD - Return DTO
   @GetMapping("/{id}")
   public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
       return ResponseEntity.ok(userService.getUserById(id));
   }
   ```

3. **Anemic Services**: Don't create services that are just pass-through to repositories
   ```java
   // ❌ BAD - Anemic service
   public Product getProduct(Long id) {
       return productRepository.findById(id).orElseThrow();
   }
   
   // ✅ GOOD - Service adds value
   public ProductDTO getProductById(Long id) {
       Product product = productRepository.findById(id)
           .orElseThrow(() -> new ResourceNotFoundException("Product", id));
       return productMapper.toDTO(product);
   }
   ```

4. **Inconsistent Exception Handling**: Don't handle exceptions individually in each method
   ```java
   // ❌ BAD - Repetitive exception handling
   @GetMapping("/{id}")
   public ResponseEntity<?> getProduct(@PathVariable Long id) {
       try {
           return ResponseEntity.ok(productService.getProductById(id));
       } catch (Exception e) {
           return ResponseEntity.status(500).body("Error: " + e.getMessage());
       }
   }
   
   // ✅ GOOD - Use @ControllerAdvice
   @GetMapping("/{id}")
   public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
       return ResponseEntity.ok(productService.getProductById(id));
   }
   ```

### Testing

Comprehensive integration tests are provided in `src/test/java/com/designpatterns/showcase/mvc/`:

- **ProductControllerTest**: Tests all product endpoints, validation, and error handling
- **UserControllerTest**: Tests user CRUD operations and business rules
- **OrderControllerTest**: Tests order creation, status updates, and stock management
- **GlobalExceptionHandlerTest**: Tests exception handling scenarios

Tests use:
- **MockMvc**: For testing REST endpoints
- **@Transactional**: For test data isolation
- **@ActiveProfiles("test")**: For test-specific configuration
- **H2 Database**: In-memory database for fast tests

### Sample Data

The application seeds sample data on startup via `DataSeeder`:
- 3 users (admin and regular users)
- 6 products across different categories
- 1 sample order with order items

### Running the MVC Showcase

```bash
# Start the application
mvn spring-boot:run

# Test the endpoints
curl http://localhost:8080/api/products
curl http://localhost:8080/api/users
curl http://localhost:8080/api/orders

# Create a new product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"New Product","price":99.99,"category":"ELECTRONICS","stockQuantity":50}'

# Run integration tests
mvn test -Dtest=*ControllerTest
```

## Design Patterns

This project demonstrates the following design patterns:

### Creational Patterns
- **Factory Method** ✅ - Implemented in `factory/` package
  - PaymentProcessorFactory with Credit Card, PayPal, and Cryptocurrency processors
  - NotificationFactory with Email, SMS, and Push notification services
  - Sample OrderProcessingService demonstrating runtime factory usage
  - See [Factory Pattern README](src/main/java/com/designpatterns/showcase/factory/README.md)
- Singleton
- Abstract Factory
- Builder
- Prototype

### Structural Patterns
- Adapter
- Bridge
- Composite
- Decorator
- Facade
- Proxy

### Behavioral Patterns
- **Template Method** ✅ - Implemented in `templatemethod/` package
  - OrderWorkflow with Standard and Express implementations
  - PaymentWorkflow with Credit Card and Cryptocurrency support
  - DataImportExportWorkflow with Database (JdbcTemplate) and CSV implementations
  - Complete with hook methods, validation, and H2 database integration
  - See [Template Method Pattern README](templatemethod/README.md)
- **Observer** ✅ - Implemented in `observer/` package
  - Domain events for orders (created, shipped, delivered) and user registration
  - ApplicationEventPublisher and @EventListener implementation
  - Asynchronous event processing with @Async and custom thread pool
  - Event chaining demonstration (welcome email → profile creation → external notification)
  - Comprehensive testing with @RecordApplicationEvents and Awaitility
  - See [Observer Pattern README](observer/README.md)
- Chain of Responsibility
- Command
- Iterator
- Mediator
- Strategy
- Visitor

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or feedback, please open an issue in the repository.
