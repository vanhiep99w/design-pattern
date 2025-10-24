# Observer Pattern with Spring Events

This package demonstrates the Observer pattern using Spring's event-driven architecture with `ApplicationEventPublisher` and `@EventListener`. It showcases both synchronous and asynchronous event handling, event chaining, and proper error handling.

## Overview

The Observer pattern defines a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically. Spring's event mechanism provides a clean implementation of this pattern.

## Architecture

### Components

```
┌─────────────────────────────────────────────────────────────────┐
│                        Service Layer                             │
│  ┌──────────────────────┐    ┌──────────────────────┐          │
│  │ ObserverOrderService │    │ ObserverUserService  │          │
│  │  - createOrder()     │    │  - registerUser()    │          │
│  │  - shipOrder()       │    │                      │          │
│  │  - deliverOrder()    │    │                      │          │
│  └──────────┬───────────┘    └──────────┬───────────┘          │
└─────────────┼──────────────────────────┼─────────────────────────┘
              │                          │
              │ publishEvent()           │ publishEvent()
              ▼                          ▼
┌─────────────────────────────────────────────────────────────────┐
│               ApplicationEventPublisher                          │
│                    (Spring Context)                              │
└─────────────────────────────────────────────────────────────────┘
              │                          │
              │ notify listeners         │ notify listeners
              ▼                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Event Listeners                            │
│  ┌──────────────────────┐    ┌──────────────────────┐          │
│  │ OrderEventListener   │    │UserRegistrationList. │          │
│  │  @EventListener      │    │  @EventListener      │          │
│  │  @Async (optional)   │    │  @Async (optional)   │          │
│  └──────────────────────┘    └──────────────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

## Domain Events

### Order Events

1. **OrderCreatedEvent** - Published when a new order is created
   - Contains: orderId, userId, totalAmount, orderDate
   - Triggers: email confirmation, warehouse notification

2. **OrderShippedEvent** - Published when an order is shipped
   - Contains: orderId, userId, trackingNumber, shippedDate
   - Triggers: shipping notification

3. **OrderDeliveredEvent** - Published when an order is delivered
   - Contains: orderId, userId, deliveredDate
   - Triggers: feedback request

### User Events

1. **UserRegisteredEvent** - Published when a user registers
   - Contains: userId, username, email, registrationDate
   - Triggers: welcome email, profile creation, external system notification, preference setup

## Event Flow Diagrams

### Order Creation Flow

```
Client                ObserverOrderService    ApplicationEventPublisher    OrderEventListener
  │                            │                        │                         │
  ├─createOrder()──────────────>│                        │                         │
  │                            │                        │                         │
  │                            ├─publishEvent()────────>│                         │
  │                            │                        │                         │
  │                            │                        ├─notify────────────────>│
  │                            │                        │                         │
  │                            │                        │    handleOrderCreated() │
  │                            │                        │    (synchronous)        │
  │                            │                        │                         │
  │                            │                        ├─notify────────────────>│
  │                            │                        │                         │
  │                            │                        │    sendConfirmationEmail()
  │                            │                        │    @Async (async thread)│
  │<──orderId──────────────────┤                        │                         │
  │                            │                        ├─notify────────────────>│
  │                            │                        │                         │
  │                            │                        │    notifyWarehouse()    │
  │                            │                        │    @Async (async thread)│
  │                            │                        │                         │
```

### User Registration with Event Chaining

```
Client           ObserverUserService    EventPublisher    UserRegistrationListener
  │                     │                    │                      │
  ├─registerUser()─────>│                    │                      │
  │                     │                    │                      │
  │                     ├─publishEvent()────>│                      │
  │                     │ (UserRegistered)   │                      │
  │                     │                    ├─notify──────────────>│
  │                     │                    │                      │
  │<──userId────────────┤                    │  handleUserRegistered()
  │                     │                    │  (synchronous)       │
  │                     │                    │                      │
  │                     │                    ├─notify──────────────>│
  │                     │                    │                      │
  │                     │                    │  sendWelcomeEmail()  │
  │                     │                    │  @Async              │
  │                     │                    │                      ├─┐
  │                     │                    │                      │ │ publishEvent()
  │                     │                    │                      │ │ (ProfileCreation)
  │                     │                    │                      │<┘
  │                     │                    │                      │
  │                     │                    ├─notify──────────────>│
  │                     │                    │                      │
  │                     │                    │  createUserProfile() │
  │                     │                    │  @Async              │
  │                     │                    │                      ├─┐
  │                     │                    │                      │ │ publishEvent()
  │                     │                    │                      │ │ (ExternalNotification)
  │                     │                    │                      │<┘
  │                     │                    │                      │
  │                     │                    ├─notify──────────────>│
  │                     │                    │                      │
  │                     │                    │  notifyExternalSystem()
  │                     │                    │  @Async              │
```

## Asynchronous Configuration

### Thread Pool Configuration

```java
@Configuration
@EnableAsync
public class AsyncEventConfig implements AsyncConfigurer {
    
    @Bean(name = "eventTaskExecutor")
    public Executor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("event-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
```

### Configuration Parameters

- **corePoolSize**: 5 - Number of threads always kept alive
- **maxPoolSize**: 10 - Maximum number of threads
- **queueCapacity**: 25 - Queue size for pending tasks
- **threadNamePrefix**: "event-async-" - For easy identification in logs
- **waitForTasksToCompleteOnShutdown**: true - Graceful shutdown
- **awaitTerminationSeconds**: 60 - Time to wait for task completion on shutdown

## Benefits of Event-Driven Architecture

### ✅ Decoupling
- **Publishers** don't know about subscribers
- Services remain focused on their primary responsibility
- Easy to add/remove listeners without changing publisher code

### ✅ Scalability
- Asynchronous processing doesn't block the main thread
- Multiple listeners can process events concurrently
- Better resource utilization with thread pools

### ✅ Extensibility
- New functionality can be added by creating new listeners
- No need to modify existing services
- Supports Open/Closed Principle (open for extension, closed for modification)

### ✅ Testability
- Easy to test publishers and listeners independently
- Spring provides `@RecordApplicationEvents` for event testing
- Can verify events are published and handled correctly

### ✅ Maintainability
- Clear separation of concerns
- Each listener handles a specific aspect
- Changes in one listener don't affect others

## Asynchronous Considerations

### When to Use @Async

✅ **Good candidates for async:**
- Sending emails/notifications
- Logging to external systems
- Updating caches
- Non-critical operations
- Operations that can tolerate eventual consistency

❌ **Avoid async for:**
- Operations requiring immediate feedback
- Database transactions that must be atomic with the main operation
- Operations where ordering is critical
- Error handling that affects the main flow

### Async Best Practices

1. **Thread Pool Configuration**
   ```java
   @Async("eventTaskExecutor")  // Use named executor
   public void handleEvent(Event event) {
       // Processing logic
   }
   ```

2. **Error Handling**
   ```java
   @Override
   public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
       return (throwable, method, params) -> {
           log.error("Uncaught async exception in {}", method.getName(), throwable);
           // Implement recovery logic or alerting
       };
   }
   ```

3. **Monitoring**
   ```java
   log.info("Thread: {}", Thread.currentThread().getName());
   ```

## Pitfalls and Solutions

### 1. Lost Events

**Problem**: If async task fails, event is lost.

**Solution**:
```java
@EventListener
@Async
@Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
public void handleEvent(Event event) {
    // Processing with automatic retry
}
```

### 2. Transaction Boundaries

**Problem**: Event published inside transaction but transaction rolls back.

**Solution**: Use `@TransactionalEventListener`
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleEvent(Event event) {
    // Only executes after successful commit
}
```

### 3. Event Ordering

**Problem**: Async events may execute out of order.

**Solution**: Use `@Order` annotation or synchronous processing for ordered events
```java
@EventListener
@Order(1)
public void firstHandler(Event event) { }

@EventListener
@Order(2)
public void secondHandler(Event event) { }
```

### 4. Memory Leaks

**Problem**: Events holding large objects or listeners accumulating.

**Solution**:
- Keep event objects lightweight
- Use proper executor configuration
- Set `waitForTasksToCompleteOnShutdown(true)`

### 5. Error Propagation

**Problem**: Async errors don't propagate to caller.

**Solution**: Implement custom `AsyncUncaughtExceptionHandler`
```java
@Override
public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (throwable, method, params) -> {
        // Log error
        // Send alert
        // Store in dead letter queue
    };
}
```

### 6. Testing Async Events

**Problem**: Tests complete before async events finish.

**Solution**: Use Awaitility for async assertions
```java
@Test
void testAsyncEvent() {
    service.publishEvent();
    
    await().atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {
            // Assertions here
        });
}
```

## Testing

### Spring Event Testing Utilities

Spring provides `@RecordApplicationEvents` annotation to capture events during tests:

```java
@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
class ObserverOrderServiceTest {

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void testEventIsPublished() {
        // Trigger event
        orderService.createOrder(userId, amount);
        
        // Verify event
        long eventCount = applicationEvents.stream(OrderCreatedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        
        // Get event details
        OrderCreatedEvent event = applicationEvents.stream(OrderCreatedEvent.class)
            .findFirst()
            .orElseThrow();
        
        assertThat(event.getOrderId()).isNotNull();
    }
}
```

### Testing Async Execution

Use Awaitility for async event testing:

```java
@Test
void testAsyncListenersExecute() {
    orderService.createOrder(userId, amount);
    
    await().atMost(3, TimeUnit.SECONDS)
        .untilAsserted(() -> {
            long eventCount = applicationEvents.stream(OrderCreatedEvent.class).count();
            assertThat(eventCount).isGreaterThanOrEqualTo(1);
        });
}
```

### Testing Event Chaining

```java
@Test
void testEventChaining() {
    Long userId = userService.registerUser(username, email);
    
    await().atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {
            // Verify initial event
            assertThat(applicationEvents.stream(UserRegisteredEvent.class).count())
                .isEqualTo(1);
            
            // Verify chained events
            assertThat(applicationEvents.stream(UserProfileCreationEvent.class).count())
                .isGreaterThanOrEqualTo(1);
            
            assertThat(applicationEvents.stream(ExternalSystemNotificationEvent.class).count())
                .isGreaterThanOrEqualTo(1);
        });
}
```

## Running the Examples

### Running Tests

```bash
# Run all observer pattern tests
mvn test -Dtest=*Observer*

# Run specific test class
mvn test -Dtest=ObserverOrderServiceTest

# Run with detailed logging
mvn test -Dtest=ObserverUserServiceTest -X
```

### Example Output

```
2024-01-15 10:30:00.123 INFO  [main] ObserverOrderService : Creating order: OrderId=1705315800123, UserId=1, Amount=99.99
2024-01-15 10:30:00.125 INFO  [main] OrderEventListener : Order created: OrderId=1705315800123, UserId=1, Amount=99.99
2024-01-15 10:30:00.125 INFO  [main] OrderEventListener : Thread: main
2024-01-15 10:30:00.127 INFO  [event-async-1] OrderEventListener : Sending order confirmation email for OrderId=1705315800123
2024-01-15 10:30:00.127 INFO  [event-async-1] OrderEventListener : Async Thread: event-async-1
2024-01-15 10:30:00.128 INFO  [event-async-2] OrderEventListener : Notifying warehouse for OrderId=1705315800123
2024-01-15 10:30:00.128 INFO  [event-async-2] OrderEventListener : Async Thread: event-async-2
```

## Key Takeaways

1. **Spring Events** provide a clean implementation of the Observer pattern
2. **@EventListener** makes it easy to react to domain events
3. **@Async** enables non-blocking event processing
4. **Event chaining** allows complex workflows to be broken down into discrete steps
5. **Testing utilities** (`@RecordApplicationEvents`, Awaitility) make event testing straightforward
6. **Proper configuration** of thread pools and error handlers is crucial for production use
7. **Be mindful** of transaction boundaries, event ordering, and error propagation

## Further Reading

- [Spring Framework Events](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events)
- [Spring @Async](https://docs.spring.io/spring-framework/reference/integration/scheduling.html#scheduling-annotation-support-async)
- [Observer Pattern](https://refactoring.guru/design-patterns/observer)
- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)
