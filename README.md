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
│   │   │   ├── domain/                       # Domain entities (User, Product)
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   ├── repository/                   # JPA repositories
│   │   │   └── data/                         # Sample data loader
│   │   ├── creational/                       # Creational patterns
│   │   ├── structural/                       # Structural patterns
│   │   └── behavioral/                       # Behavioral patterns
│   └── resources/
│       ├── application.properties            # Main configuration
│       ├── application-dev.properties        # Development profile
│       └── application-prod.properties       # Production profile
└── test/
    └── java/com/designpatterns/showcase/
        └── ...                               # Test classes
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

## Design Patterns

This project will demonstrate the following design patterns (to be implemented):

### Creational Patterns
- Singleton
- Factory Method
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
- Chain of Responsibility
- Command
- Iterator
- Mediator
- Observer
- Strategy
- Template Method
- Visitor

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or feedback, please open an issue in the repository.
