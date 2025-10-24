# Decorator Pattern Implementation

## Overview

The Decorator pattern allows you to add new functionality to objects dynamically by wrapping them in decorator classes. This implementation demonstrates service decorators for a `DataService` interface with cross-cutting concerns including logging, caching, encryption, and feature toggles.

## Pattern Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                         DataService                              │
│                        (Interface)                               │
│  + save(data): String                                           │
│  + retrieve(id): Optional<String>                               │
│  + findAll(): List<String>                                      │
│  + delete(id): boolean                                          │
│  + clearCache(): void                                           │
└──────────────┬──────────────────────────────────────────────────┘
               │                                       
               │ implements                            
               │                                       
      ┌────────┴─────────┐                            
      │                  │                            
      ▼                  ▼                            
┌──────────────┐  ┌──────────────────────────────────┐
│SimpleData    │  │ DataServiceDecorator             │
│Service       │  │ (Abstract Base)                  │
└──────────────┘  │ - delegate: DataService          │
                  └────────┬─────────────────────────┘
                           │ extends
                ┌──────────┼──────────┬──────────────┐
                │          │          │              │
                ▼          ▼          ▼              ▼
        ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌───────────┐
        │ Logging  │ │ Caching  │ │Encryption │ │  Feature  │
        │Decorator │ │Decorator │ │Decorator  │ │  Toggle   │
        └──────────┘ └──────────┘ └───────────┘ └───────────┘
```

## Components

### 1. Core Interface: `DataService`

Defines the contract for data operations:
- `save(data)`: Persist data and return generated ID
- `retrieve(id)`: Fetch data by ID
- `findAll()`: Retrieve all stored data
- `delete(id)`: Remove data by ID
- `clearCache()`: Clear any cached data

### 2. Base Implementation: `SimpleDataService`

A concrete implementation using in-memory storage (`ConcurrentHashMap`) with thread-safe operations.

### 3. Decorators

#### LoggingDataServiceDecorator
Adds comprehensive logging around all operations:
- Logs method entry and exit
- Records execution time
- Logs success/failure status
- Truncates long data in logs

#### CachingDataServiceDecorator
Implements time-based caching:
- Configurable TTL (Time To Live)
- Cache hit/miss tracking
- Cache statistics (hit rate, size)
- Automatic cache eviction on delete

#### EncryptionDataServiceDecorator
Encrypts data before storage:
- XOR-based encryption with Base64 encoding
- Configurable encryption key
- Transparent encryption/decryption
- Supports Unicode and special characters

#### FeatureToggleDataServiceDecorator
Enables runtime feature toggling:
- Enable/disable operations dynamically
- Graceful degradation when disabled
- Named features for identification
- Throws `FeatureDisabledException` when disabled

## Decorator Stacking

### Layer Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                           Client                                  │
└─────────────────────────────┬────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  Layer 4: Feature Toggle Decorator                               │
│  - Controls access to all operations                             │
│  - Can disable entire service at runtime                         │
└─────────────────────────────┬────────────────────────────────────┘
                              │ delegates to
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  Layer 3: Encryption Decorator                                   │
│  - Encrypts data before saving                                   │
│  - Decrypts data after retrieval                                 │
└─────────────────────────────┬────────────────────────────────────┘
                              │ delegates to
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  Layer 2: Caching Decorator                                      │
│  - Caches encrypted data (encrypted at rest)                     │
│  - Improves performance for repeated reads                       │
│  - TTL-based expiration                                          │
└─────────────────────────────┬────────────────────────────────────┘
                              │ delegates to
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  Layer 1: Logging Decorator                                      │
│  - Logs all operations with timing                               │
│  - Logs exceptions and errors                                    │
│  - Tracks performance metrics                                    │
└─────────────────────────────┬────────────────────────────────────┘
                              │ delegates to
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  Base: SimpleDataService                                         │
│  - Core in-memory storage                                        │
│  - Thread-safe operations                                        │
└──────────────────────────────────────────────────────────────────┘
```

### Execution Flow Example

When a client calls `save("Hello World")`:

1. **Feature Toggle Layer**: Checks if feature is enabled
2. **Encryption Layer**: Encrypts "Hello World" → "ZW5jcnlwdGVkLi4u"
3. **Caching Layer**: Stores encrypted value in cache
4. **Logging Layer**: Logs operation with timing
5. **Base Service**: Stores encrypted data with generated ID

When retrieving the same data with `retrieve(id)`:

1. **Feature Toggle Layer**: Checks if feature is enabled
2. **Encryption Layer**: Prepares to decrypt
3. **Caching Layer**: Returns cached encrypted value (cache hit!)
4. **Encryption Layer**: Decrypts → "Hello World"
5. **Logging Layer**: Logs cache hit
6. Client receives: "Hello World"

## Spring Configuration

### Configuration Strategies

#### 1. Minimal Stack (Default)
```properties
decorator.stack=minimal
```
Provides only logging functionality.

#### 2. Caching Only Stack
```properties
decorator.stack=caching-only
decorator.caching.ttl=60000
```
Logging + Caching for improved performance.

#### 3. Secure Stack
```properties
decorator.stack=secure
decorator.encryption.key=my-secret-key-12345
```
Logging + Encryption for data security.

#### 4. Full Stack
```properties
decorator.stack=full
decorator.caching.ttl=60000
decorator.encryption.key=my-secret-key-12345
decorator.feature-toggle.feature-name=data-service
decorator.feature-toggle.initially-enabled=true
```
All decorators stacked for maximum functionality.

### Using @Primary and @Qualifier

The `DecoratorConfiguration` class uses Spring's `@Primary` and `@Qualifier` annotations to compose decorators:

```java
@Bean
@Primary
@ConditionalOnProperty(name = "decorator.stack", havingValue = "full")
public DataService fullyDecoratedDataService(
        @Qualifier("simpleDataService") DataService baseService,
        @Value("${decorator.caching.ttl:60000}") long ttlMillis,
        @Value("${decorator.encryption.key:default-secret-key}") String encryptionKey) {
    
    DataService service = baseService;
    service = new LoggingDataServiceDecorator(service);
    service = new CachingDataServiceDecorator(service, ttlMillis);
    service = new EncryptionDataServiceDecorator(service, encryptionKey);
    service = new FeatureToggleDataServiceDecorator(service, "data-service", true);
    
    return service;
}
```

### Runtime Configuration

You can toggle features at runtime through the REST API:

```bash
# Enable feature
curl -X POST http://localhost:8080/api/decorator/feature-toggle \
  -H "Content-Type: application/json" \
  -d '{"enable": true}'

# Disable feature
curl -X POST http://localhost:8080/api/decorator/feature-toggle \
  -H "Content-Type: application/json" \
  -d '{"enable": false}'
```

## REST API Examples

### Save Data
```bash
curl -X POST http://localhost:8080/api/decorator/data \
  -H "Content-Type: application/json" \
  -d '{"data": "Hello World"}'
```

Response:
```json
{
  "id": "ID-1",
  "message": "Data saved successfully"
}
```

### Retrieve Data
```bash
curl http://localhost:8080/api/decorator/data/ID-1
```

Response:
```json
{
  "id": "ID-1",
  "data": "Hello World"
}
```

### Get All Data
```bash
curl http://localhost:8080/api/decorator/data
```

Response:
```json
[
  "Hello World",
  "Test Data",
  "Another Entry"
]
```

### Delete Data
```bash
curl -X DELETE http://localhost:8080/api/decorator/data/ID-1
```

Response:
```json
{
  "id": "ID-1",
  "deleted": true
}
```

### Clear Cache
```bash
curl -X POST http://localhost:8080/api/decorator/cache/clear
```

### Get Cache Statistics
```bash
curl http://localhost:8080/api/decorator/cache/stats
```

Response:
```json
{
  "cacheSize": 5,
  "cacheHits": 23,
  "cacheMisses": 7,
  "cacheHitRate": 0.7666666666666667
}
```

## Best Practices

### ✅ DO:

1. **Order Matters**: Stack decorators in the right order
   ```java
   // Good: Encrypt before caching (cache encrypted data)
   service = new EncryptionDataServiceDecorator(service, key);
   service = new CachingDataServiceDecorator(service, ttl);
   ```

2. **Use Interface Delegation**: Always delegate to the interface, not concrete classes
   ```java
   private final DataService delegate;  // Good
   private final SimpleDataService delegate;  // Bad
   ```

3. **Keep Decorators Focused**: Each decorator should have a single responsibility
   ```java
   // Good: Separate concerns
   LoggingDataServiceDecorator
   CachingDataServiceDecorator
   
   // Bad: Mixed concerns
   LoggingAndCachingDataServiceDecorator
   ```

4. **Make Decorators Reusable**: Don't couple decorators to specific implementations
   ```java
   // Good: Works with any DataService
   public LoggingDataServiceDecorator(DataService delegate)
   
   // Bad: Coupled to specific implementation
   public LoggingDataServiceDecorator(SimpleDataService delegate)
   ```

### ❌ AVOID:

1. **Complex Dependency Graphs**: Too many decorators can create confusion
   ```java
   // Bad: Too many layers, hard to debug
   service = new Decorator1(new Decorator2(new Decorator3(
             new Decorator4(new Decorator5(new Decorator6(base))))));
   ```
   **Solution**: Group related decorators or use configuration profiles.

2. **Circular Dependencies**: Decorators should not depend on each other
   ```java
   // Bad: Circular reference
   DecoratorA depends on DecoratorB
   DecoratorB depends on DecoratorA
   ```

3. **Breaking the Chain**: Always delegate to the wrapped service
   ```java
   // Bad: Breaking the chain
   @Override
   public String save(String data) {
       // Do something but forget to call delegate
       return "ID-1";  // Wrong!
   }
   
   // Good: Maintain the chain
   @Override
   public String save(String data) {
       // Add behavior
       return delegate.save(data);  // Correct!
   }
   ```

4. **Exposing Decorator Types**: Don't expose concrete decorator types in APIs
   ```java
   // Bad: Exposing concrete type
   public CachingDataServiceDecorator getService()
   
   // Good: Return interface
   public DataService getService()
   ```

## Common Pitfalls

### 1. Wrong Decorator Order

**Problem**: Caching before encryption exposes plain text in cache
```java
// BAD: Cache stores plain text
service = new CachingDataServiceDecorator(service, ttl);
service = new EncryptionDataServiceDecorator(service, key);
```

**Solution**: Encrypt before caching
```java
// GOOD: Cache stores encrypted data
service = new EncryptionDataServiceDecorator(service, key);
service = new CachingDataServiceDecorator(service, ttl);
```

### 2. State Management in Decorators

**Problem**: Decorators holding state can cause issues with different decorator instances

**Solution**: Keep decorators stateless when possible, or manage state carefully:
```java
// Acceptable: State for caching is the decorator's purpose
private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

// Acceptable: State for metrics
private int cacheHits = 0;
private int cacheMisses = 0;
```

### 3. Testing Decorated Services

**Problem**: Difficult to test individual decorator behavior

**Solution**: Test each decorator individually, then test stacking:
```java
@Test
void shouldTestLoggingAlone() {
    DataService base = new SimpleDataService();
    DataService logging = new LoggingDataServiceDecorator(base);
    // Test only logging behavior
}

@Test
void shouldTestFullStack() {
    // Test complete decorator stack
    DataService service = buildFullStack();
    // Test end-to-end behavior
}
```

### 4. Spring Bean Conflicts

**Problem**: Multiple beans of the same type without proper qualification

**Solution**: Use `@Primary` and `@Qualifier` annotations:
```java
@Bean
@Primary
public DataService primaryService() { ... }

@Bean
@Qualifier("cachingOnly")
public DataService cachingService() { ... }
```

## Performance Considerations

### Caching Effectiveness

The caching decorator tracks hit rate:
- **High hit rate (>70%)**: Caching is effective
- **Low hit rate (<30%)**: Consider adjusting TTL or disabling cache

### Decorator Overhead

Each decorator adds minimal overhead:
- **Logging**: ~1-2ms per operation
- **Caching**: ~0.1ms (cache hit), ~1ms (cache miss)
- **Encryption**: ~2-5ms depending on data size
- **Feature Toggle**: ~0.1ms

### Optimization Tips

1. **Order for Performance**: Place frequently-bypassing decorators first
2. **Cache TTL Tuning**: Balance freshness vs. performance
3. **Selective Decoration**: Don't add decorators you don't need

## Testing

The implementation includes comprehensive tests:

### Unit Tests
- `SimpleDataServiceTest`: Base service functionality
- `LoggingDataServiceDecoratorTest`: Logging via Logback appender
- `CachingDataServiceDecoratorTest`: Cache effectiveness and hit rates
- `EncryptionDataServiceDecoratorTest`: Encryption/decryption correctness
- `FeatureToggleDataServiceDecoratorTest`: Toggle behavior

### Integration Tests
- `DecoratorStackingTest`: Multiple decorators working together

### Running Tests
```bash
mvn test -Dtest=*DecoratorTest
```

## When to Use the Decorator Pattern

### ✅ Good Use Cases:
- Adding cross-cutting concerns (logging, caching, security)
- Runtime behavior modification
- Combining multiple independent features
- Following Open/Closed Principle (open for extension, closed for modification)

### ❌ Not Suitable For:
- Simple inheritance would suffice
- Performance-critical code with minimal overhead requirements
- When the number of combinations grows exponentially
- When decorators have complex interdependencies

## Comparison with Other Patterns

| Pattern | Purpose | When to Use |
|---------|---------|-------------|
| **Decorator** | Add responsibilities dynamically | Runtime behavior enhancement |
| **Proxy** | Control access to an object | Lazy loading, access control, remote calls |
| **Adapter** | Convert one interface to another | Integrating incompatible interfaces |
| **Chain of Responsibility** | Pass request through handlers | Request processing pipeline |

## Summary

The Decorator pattern provides a flexible alternative to subclassing for extending functionality. This implementation demonstrates:

- ✅ Clean separation of concerns
- ✅ Runtime composition via Spring configuration
- ✅ Comprehensive logging, caching, encryption, and feature toggling
- ✅ Extensive test coverage
- ✅ Production-ready error handling
- ✅ Performance monitoring and metrics

The key to successful decorator usage is thoughtful ordering, focused responsibilities, and clear documentation of decorator stacks.
