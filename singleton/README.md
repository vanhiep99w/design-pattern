# Singleton Pattern

A comprehensive implementation of the Singleton design pattern with thread-safety demonstrations, Spring bean scopes comparison, and production-ready examples.

## Table of Contents
- [Overview](#overview)
- [Implementation Approaches](#implementation-approaches)
- [Examples](#examples)
- [Spring Bean Scopes](#spring-bean-scopes)
- [Thread Safety](#thread-safety)
- [Benefits](#benefits)
- [Pitfalls](#pitfalls)
- [Testing](#testing)
- [Usage](#usage)

## Overview

The Singleton pattern ensures that a class has only one instance throughout the application lifecycle and provides a global point of access to that instance. This pattern is particularly useful for:

- Configuration management
- Connection pooling
- Caching
- Logging
- Resource managers

## Implementation Approaches

### 1. Initialization-on-Demand Holder (Bill Pugh Singleton)

**Implementation**: `ConfigurationManager`

This approach uses a static inner class to hold the singleton instance, leveraging the Java class loading mechanism for thread-safe lazy initialization.

```java
public class ConfigurationManager {
    private ConfigurationManager() {
        // Private constructor
    }

    public static ConfigurationManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final ConfigurationManager INSTANCE = new ConfigurationManager();
    }
}
```

**Advantages**:
- Thread-safe without synchronization overhead
- Lazy initialization
- No explicit locking required
- Simple and clean code

**How it works**:
- The inner `Holder` class is not loaded until `getInstance()` is called
- Class loading is inherently thread-safe in Java
- The JVM guarantees that the static initializer runs only once

### 2. Double-Checked Locking with Volatile

**Implementation**: `DatabaseConnectionPool`

This approach uses double-checked locking to minimize synchronization overhead while ensuring thread safety.

```java
public class DatabaseConnectionPool {
    private static volatile DatabaseConnectionPool instance;

    private DatabaseConnectionPool() {
        // Private constructor
    }

    public static DatabaseConnectionPool getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionPool();
                }
            }
        }
        return instance;
    }
}
```

**Key points**:
- `volatile` keyword ensures visibility of changes across threads
- First check avoids synchronization overhead after initialization
- Second check (inside synchronized block) ensures only one instance is created
- Prevents partial initialization due to instruction reordering

**Why volatile is essential**:
Without `volatile`, the following can happen:
1. Thread A enters synchronized block
2. Thread A allocates memory for instance
3. Thread A partially initializes the object
4. Thread A assigns reference to instance
5. Thread B sees instance != null but object is partially initialized

The `volatile` keyword prevents instruction reordering and ensures all threads see the fully initialized object.

## Examples

### ConfigurationManager

A centralized configuration manager using the Initialization-on-Demand Holder pattern.

**Features**:
- Thread-safe configuration storage
- Default settings loaded at initialization
- ConcurrentHashMap for concurrent access
- Immutable settings view via `getAllSettings()`

**Use Case**: Managing application-wide settings that need to be accessed from multiple components.

```java
ConfigurationManager config = ConfigurationManager.getInstance();
String appName = config.getSetting("app.name");
config.setSetting("cache.enabled", "true");
```

### DatabaseConnectionPool

A connection pool manager using double-checked locking for lazy initialization.

**Features**:
- Fixed-size connection pool
- BlockingQueue for thread-safe connection management
- Connection timeout handling
- Atomic counters for active connections
- Initialization control

**Use Case**: Managing database connections efficiently across the application.

```java
DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance();
pool.initialize();

PooledConnection conn = pool.getConnection();
conn.executeQuery("SELECT * FROM users");
pool.releaseConnection(conn);
```

## Spring Bean Scopes

Spring provides built-in singleton management through bean scopes. This is different from the classic Singleton pattern.

### Singleton Scope (Default)

```java
@Bean
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public ApplicationCacheService singletonCacheService() {
    return new ApplicationCacheService();
}
```

**Characteristics**:
- One instance per Spring container
- Instance created at container startup (eager) or first access (lazy)
- Shared state across all injection points
- Thread-safe by container management

**Diagram**:
```
┌─────────────────────────────────────────┐
│      Spring Application Context         │
│                                         │
│  ┌───────────────────────────────┐     │
│  │ ApplicationCacheService Bean  │     │
│  │   (SINGLETON SCOPE)           │     │
│  └───────┬───────────────┬───────┘     │
│          │               │             │
└──────────┼───────────────┼─────────────┘
           │               │
    ┌──────▼──────┐ ┌─────▼──────┐
    │ Service A   │ │ Service B  │
    │ @Autowired  │ │ @Autowired │
    └─────────────┘ └────────────┘
         ↓               ↓
    Same Instance   Same Instance
```

### Prototype Scope

```java
@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public SessionService prototypeSessionService() {
    return new SessionService();
}
```

**Characteristics**:
- New instance created every time bean is requested
- No shared state
- Each injection point gets a unique instance
- Spring doesn't manage lifecycle beyond creation

**Diagram**:
```
┌─────────────────────────────────────────┐
│      Spring Application Context         │
│                                         │
│  ┌───────────────────────────────┐     │
│  │  SessionService Bean Factory  │     │
│  │   (PROTOTYPE SCOPE)           │     │
│  └─────┬──────────────────┬──────┘     │
│        │                  │            │
└────────┼──────────────────┼────────────┘
         │                  │
         ▼                  ▼
    ┌─────────┐        ┌─────────┐
    │Instance1│        │Instance2│
    │(Unique) │        │(Unique) │
    └─────────┘        └─────────┘
```

### Comparison: Classic Singleton vs Spring Singleton

| Aspect | Classic Singleton | Spring Singleton |
|--------|------------------|------------------|
| **Scope** | JVM-wide (entire application) | Spring container-wide |
| **Creation** | Manual (getInstance()) | Managed by Spring IoC |
| **Testing** | Difficult (global state) | Easy (mockable/injectable) |
| **Flexibility** | Hard-coded dependency | Configurable via DI |
| **Lifecycle** | Self-managed | Container-managed |
| **Thread Safety** | Must implement manually | Container ensures |
| **Serialization** | Requires special handling | Spring handles |

**When to use Classic Singleton**:
- Low-level infrastructure code
- No Spring context available
- Performance-critical code (minimal overhead)
- Libraries/frameworks

**When to use Spring Singleton**:
- Application services and components
- When dependency injection is needed
- When testing is important
- Business logic layers

## Thread Safety

### Why Thread Safety Matters

Without proper synchronization, multiple threads can:
1. Create multiple instances simultaneously
2. Access partially initialized objects
3. See stale values due to CPU caching
4. Cause race conditions

### Thread-Safe Initialization Techniques

#### 1. Initialization-on-Demand Holder (Recommended)
```java
private static class Holder {
    private static final ConfigurationManager INSTANCE = new ConfigurationManager();
}
```
- **No synchronization needed**
- JVM class loading mechanism guarantees thread safety
- Lazy initialization
- Best performance

#### 2. Double-Checked Locking
```java
if (instance == null) {
    synchronized (DatabaseConnectionPool.class) {
        if (instance == null) {
            instance = new DatabaseConnectionPool();
        }
    }
}
```
- **Minimal synchronization**
- Volatile keyword prevents instruction reordering
- Lazy initialization
- Good for complex initialization

#### 3. Eager Initialization
```java
private static final Singleton INSTANCE = new Singleton();
```
- **Thread-safe by default**
- Instance created at class loading
- No lazy initialization
- Simple but wastes resources if not used

#### 4. Enum Singleton (Not shown in examples)
```java
public enum Singleton {
    INSTANCE;
}
```
- **Inherently thread-safe**
- Serialization-safe
- Prevents reflection attacks
- Best for simple singletons

### Thread Safety in Our Examples

**ConfigurationManager**:
- Uses Initialization-on-Demand Holder
- ConcurrentHashMap for concurrent reads/writes
- No explicit synchronization needed for getInstance()

**DatabaseConnectionPool**:
- Uses double-checked locking with volatile
- BlockingQueue for thread-safe connection management
- Synchronized methods for initialization and shutdown
- AtomicInteger for thread-safe counters

## Benefits

### ✅ Advantages

1. **Controlled Access**: Single point of access to shared resources
2. **Memory Efficiency**: Only one instance exists in memory
3. **Global State**: Easy access to shared data across the application
4. **Lazy Initialization**: Instance created only when needed (with some approaches)
5. **Consistent State**: All parts of application see the same data
6. **Resource Management**: Ideal for expensive resources (DB pools, caches)

### Use Cases

- **Configuration Management**: Application-wide settings
- **Connection Pools**: Database, HTTP, thread pools
- **Caching**: Application-level cache
- **Logging**: Centralized logging service
- **Hardware Access**: Printer, file system managers
- **Registry/Factory**: Service registries, object factories

## Pitfalls

### ❌ Disadvantages and Anti-patterns

#### 1. Global State
**Problem**: Hidden dependencies, harder to understand code flow

```java
// ❌ BAD - Hidden dependency
public class OrderService {
    public void processOrder(Order order) {
        // Hidden dependency on ConfigurationManager
        String email = ConfigurationManager.getInstance().getSetting("admin.email");
    }
}

// ✅ GOOD - Explicit dependency
public class OrderService {
    private final ConfigurationManager config;
    
    public OrderService(ConfigurationManager config) {
        this.config = config;
    }
}
```

#### 2. Testing Difficulties
**Problem**: Hard to mock, shared state between tests

```java
// ❌ BAD - Tests can interfere with each other
@Test
void test1() {
    ConfigurationManager.getInstance().setSetting("key", "value1");
    // Test logic
}

@Test
void test2() {
    // Might see "value1" from test1 if not cleaned up
    String value = ConfigurationManager.getInstance().getSetting("key");
}

// ✅ GOOD - Clear state between tests
@AfterEach
void tearDown() {
    ConfigurationManager.getInstance().clearSettings();
}
```

#### 3. Tight Coupling
**Problem**: Classes become tightly coupled to singleton implementation

```java
// ❌ BAD - Tight coupling
public class PaymentService {
    public void processPayment() {
        DatabaseConnectionPool.getInstance().getConnection();
    }
}

// ✅ GOOD - Dependency injection
public class PaymentService {
    private final ConnectionPool connectionPool;
    
    public PaymentService(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
}
```

#### 4. Violates Single Responsibility Principle
**Problem**: Class manages both its business logic AND its lifecycle

The class is responsible for:
- Its own instantiation
- Its business logic
- Preventing multiple instances

#### 5. Concurrency Issues
**Problem**: Shared mutable state can cause race conditions

```java
// ❌ BAD - Race condition
public class Counter {
    private int count = 0;
    
    public void increment() {
        count++; // Not thread-safe!
    }
}

// ✅ GOOD - Thread-safe
public class Counter {
    private final AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();
    }
}
```

#### 6. Serialization Issues
**Problem**: Deserialization creates new instances

```java
// ✅ GOOD - Prevent multiple instances on deserialization
protected Object readResolve() {
    return getInstance();
}
```

#### 7. Reflection Attacks
**Problem**: Reflection can bypass private constructor

```java
// Constructor can be invoked via reflection
Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
constructor.setAccessible(true);
Singleton instance = constructor.newInstance(); // Creates second instance!

// ✅ GOOD - Throw exception if already initialized
private Singleton() {
    if (Holder.INSTANCE != null) {
        throw new IllegalStateException("Already initialized");
    }
}
```

### Best Practices

1. **Prefer Dependency Injection**: Use Spring's singleton scope over classic singleton
2. **Make State Immutable**: When possible, use immutable state
3. **Use Thread-Safe Collections**: ConcurrentHashMap, CopyOnWriteArrayList, etc.
4. **Clear Testing Strategy**: Provide reset/clear methods for testing
5. **Document Thread Safety**: Clearly document thread-safety guarantees
6. **Consider Alternatives**: Sometimes a regular class with DI is better

## Testing

### Test Coverage

Our test suite includes:

1. **Singleton Semantics**:
   - Multiple calls return same instance
   - Identity hash code comparison
   - Instance reference equality

2. **Thread Safety**:
   - Concurrent initialization (100 threads)
   - Concurrent reads and writes
   - Race condition prevention

3. **Business Logic**:
   - Configuration CRUD operations
   - Connection pool operations
   - State management

4. **Spring Bean Scopes**:
   - Singleton vs Prototype comparison
   - Shared vs Independent state
   - Creation timing and lifecycle

### Running Tests

```bash
# Run all singleton tests
mvn test -Dtest=*singleton*

# Run specific test class
mvn test -Dtest=ConfigurationManagerTest
mvn test -Dtest=DatabaseConnectionPoolTest
mvn test -Dtest=SpringBeanScopeTest
```

### Test Structure

```
src/test/java/com/designpatterns/showcase/singleton/
├── ConfigurationManagerTest.java         # Classic singleton tests
├── DatabaseConnectionPoolTest.java       # Connection pool tests
└── SpringBeanScopeTest.java             # Spring scope comparison
```

## Usage

### 1. Classic Singleton - Configuration Manager

```java
// Get singleton instance
ConfigurationManager config = ConfigurationManager.getInstance();

// Read settings
String appName = config.getSetting("app.name");
String dbTimeout = config.getSetting("db.pool.timeout", "30000");

// Update settings
config.setSetting("custom.setting", "custom-value");

// Check existence
if (config.hasSetting("cache.enabled")) {
    boolean cacheEnabled = Boolean.parseBoolean(config.getSetting("cache.enabled"));
}

// Get all settings (immutable copy)
Map<String, String> allSettings = config.getAllSettings();

// Clear and reload defaults
config.clearSettings();
```

### 2. Classic Singleton - Database Connection Pool

```java
// Get singleton instance
DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance();

// Initialize pool (only once)
pool.initialize();

// Get connection
try {
    PooledConnection connection = pool.getConnection();
    connection.executeQuery("SELECT * FROM users");
    
    // Always release connection back to pool
    pool.releaseConnection(connection);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

// Check pool status
int available = pool.getAvailableConnectionCount();
int active = pool.getActiveConnectionCount();
int maxSize = pool.getMaxPoolSize();

// Shutdown pool (cleanup)
pool.shutdown();
```

### 3. Spring Singleton Bean

```java
@Service
public class MyService {
    
    @Autowired
    private ApplicationCacheService cacheService; // Singleton scope
    
    public void doSomething() {
        cacheService.put("key", "value");
        Object value = cacheService.get("key");
    }
}
```

### 4. Spring Prototype Bean

```java
@Service
public class RequestHandler {
    
    @Autowired
    private ApplicationContext context;
    
    public void handleRequest() {
        // Get new instance each time
        SessionService session = context.getBean("prototypeSessionService", SessionService.class);
        session.processRequest("data");
    }
}
```

### 5. Demonstrating Singleton Behavior

```java
@Autowired
private SingletonDemoService demoService;

// Run demonstration
demoService.demonstrateClassicSingletons();
```

## Configuration

Add to `application.properties`:

```properties
# Singleton Pattern Configuration
singleton.config.app.name=Design Pattern Showcase
singleton.config.app.version=1.0.0
singleton.config.app.environment=development
singleton.db.pool.size=10
singleton.db.pool.timeout=30000
singleton.cache.enabled=true
singleton.cache.ttl=3600
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     Singleton Pattern Examples                   │
└─────────────────────────────────────────────────────────────────┘
                              │
            ┌─────────────────┴─────────────────┐
            │                                   │
┌───────────▼──────────────┐      ┌───────────▼──────────────┐
│   Classic Singletons     │      │   Spring Bean Scopes     │
└───────────┬──────────────┘      └───────────┬──────────────┘
            │                                   │
   ┌────────┴────────┐                 ┌───────┴───────┐
   │                 │                 │               │
┌──▼──────────┐  ┌──▼─────────────┐ ┌─▼────────┐  ┌─▼────────┐
│Configuration│  │  Connection    │ │Singleton │  │Prototype │
│  Manager    │  │     Pool       │ │  Cache   │  │ Session  │
└─────────────┘  └────────────────┘ └──────────┘  └──────────┘
      │                  │                │              │
      │                  │                │              │
  (Holder)         (Double-Check)    (@Singleton)  (@Prototype)
      │                  │                │              │
      └──────────┬───────┴────────────────┘              │
                 │                                       │
           Thread-Safe                            New Instance
          Single Instance                          Every Request
```

## Conclusion

The Singleton pattern is powerful but should be used judiciously. In modern Spring applications, prefer Spring's singleton scope over classic singleton for better testability and maintainability. Use classic singletons only for low-level infrastructure code where Spring context is not available.

**Remember**: Just because you can make something a singleton doesn't mean you should!
