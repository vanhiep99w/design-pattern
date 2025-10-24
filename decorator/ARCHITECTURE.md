# Decorator Pattern Architecture

## Class Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                     <<interface>>                             │
│                      DataService                              │
├──────────────────────────────────────────────────────────────┤
│ + save(data: String): String                                 │
│ + retrieve(id: String): Optional<String>                     │
│ + findAll(): List<String>                                    │
│ + delete(id: String): boolean                                │
│ + clearCache(): void                                         │
└────────────────┬─────────────────────────────────────────────┘
                 │
                 │ implements
                 │
    ┌────────────┼────────────────────────┐
    │            │                        │
    │            │                        │
    ▼            ▼                        ▼
┌───────────────────┐          ┌──────────────────────────────┐
│ SimpleDataService │          │  DataServiceDecorator        │
├───────────────────┤          │  (abstract)                  │
│ - dataStore       │          ├──────────────────────────────┤
│ - idGenerator     │          │ # delegate: DataService      │
├───────────────────┤          ├──────────────────────────────┤
│ + save()          │          │ # DataServiceDecorator()     │
│ + retrieve()      │          │ + save()                     │
│ + findAll()       │          │ + retrieve()                 │
│ + delete()        │          │ + findAll()                  │
│ + clearCache()    │          │ + delete()                   │
└───────────────────┘          │ + clearCache()               │
                               └───────┬──────────────────────┘
                                       │
                                       │ extends
                      ┌────────────────┼────────────────┬──────────────┐
                      │                │                │              │
                      ▼                ▼                ▼              ▼
         ┌──────────────────┐ ┌─────────────┐ ┌──────────────┐ ┌────────────┐
         │    Logging       │ │   Caching   │ │  Encryption  │ │  Feature   │
         │   Decorator      │ │  Decorator  │ │  Decorator   │ │   Toggle   │
         ├──────────────────┤ ├─────────────┤ ├──────────────┤ ├────────────┤
         │ - logger         │ │ - cache     │ │ - key        │ │ - enabled  │
         │                  │ │ - ttl       │ │              │ │ - name     │
         ├──────────────────┤ ├─────────────┤ ├──────────────┤ ├────────────┤
         │ + save()         │ │ + save()    │ │ + save()     │ │ + save()   │
         │ + retrieve()     │ │ + retrieve()│ │ + retrieve() │ │ + enable() │
         │ + findAll()      │ │ + findAll() │ │ + findAll()  │ │ + disable()│
         │ + delete()       │ │ + delete()  │ │ + delete()   │ │            │
         │ + clearCache()   │ │ + getStats()│ │ + encrypt()  │ │            │
         │ - truncate()     │ │             │ │ + decrypt()  │ │            │
         └──────────────────┘ └─────────────┘ └──────────────┘ └────────────┘
```

## Sequence Diagram: Save Operation with Full Stack

```
Client          Feature         Encryption       Caching         Logging        Simple
  │             Toggle          Decorator        Decorator       Decorator      DataService
  │               │                 │                │               │              │
  │──save(data)──>│                 │                │               │              │
  │               │                 │                │               │              │
  │               │──isEnabled()?   │                │               │              │
  │               │<──true──────────┘                │               │              │
  │               │                 │                │               │              │
  │               │──save(data)────>│                │               │              │
  │               │                 │──encrypt(data)─┤               │              │
  │               │                 │<─encrypted─────┘               │              │
  │               │                 │                │               │              │
  │               │                 │──save(enc)────>│               │              │
  │               │                 │                │──cache(enc)──┤│              │
  │               │                 │                │               │              │
  │               │                 │                │──save(enc)──>│              │
  │               │                 │                │               │──log(start)─┤
  │               │                 │                │               │              │
  │               │                 │                │               │──save(enc)─>│
  │               │                 │                │               │              │
  │               │                 │                │               │              │──[store]
  │               │                 │                │               │<──id─────────│
  │               │                 │                │               │──log(end)────┤
  │               │                 │                │<──id──────────│              │
  │               │                 │<──id───────────│               │              │
  │               │<──id────────────│                │               │              │
  │<──id──────────│                 │                │               │              │
  │               │                 │                │               │              │
```

## Sequence Diagram: Retrieve Operation with Caching

```
Client          Logging         Caching         Simple
  │             Decorator       Decorator       DataService
  │                 │               │               │
  │──retrieve(id)──>│               │               │
  │                 │──log(start)──┤│               │
  │                 │               │               │
  │                 │──retrieve(id)>│               │
  │                 │               │──checkCache()┤│
  │                 │               │<──HIT!───────┘│
  │                 │<──data────────│               │
  │                 │──log(end)─────┤               │
  │<──data──────────│               │               │
  │                 │               │               │
  
  [On Cache Miss:]
  │                 │               │               │
  │──retrieve(id)──>│               │               │
  │                 │──retrieve(id)>│               │
  │                 │               │──checkCache()┤│
  │                 │               │<──MISS───────┘│
  │                 │               │               │
  │                 │               │──retrieve(id)>│
  │                 │               │               │──[load]
  │                 │               │<──data────────│
  │                 │               │──cache(data)──┤
  │                 │<──data────────│               │
  │<──data──────────│               │               │
```

## Component Interaction Matrix

| Decorator | Modifies Data | Caches Data | Logs Operations | Blocks Operations | Metrics |
|-----------|---------------|-------------|-----------------|-------------------|---------|
| Logging   | ❌            | ❌          | ✅              | ❌                | ✅      |
| Caching   | ❌            | ✅          | ❌              | ❌                | ✅      |
| Encryption| ✅            | ❌          | ❌              | ❌                | ❌      |
| Feature Toggle| ❌        | ❌          | ❌              | ✅                | ❌      |

## Decorator Stacking Orders

### Recommended Order (Full Stack)
```
Client → Feature Toggle → Encryption → Caching → Logging → Base Service
```

**Rationale:**
1. **Feature Toggle** (outermost): Can disable entire service
2. **Encryption**: Encrypts before caching (stores encrypted data)
3. **Caching**: Caches encrypted data for security
4. **Logging** (innermost decorator): Logs actual operations that reach base service

### Alternative Order: Logging First
```
Client → Feature Toggle → Logging → Encryption → Caching → Base Service
```

**Trade-offs:**
- ✅ Logs all operations including blocked ones
- ❌ More verbose logs
- ❌ Logs include encrypted data

### Anti-Pattern: Encryption After Caching
```
Client → Caching → Encryption → Logging → Base Service
```

**Problems:**
- ❌ Caches plain text data (security risk!)
- ❌ Cache doesn't work (encrypts differently each time with timestamp)

## Spring Bean Wiring

```
┌────────────────────────────────────────────────────────────┐
│              DecoratorConfiguration                         │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  @Bean("simpleDataService")                                │
│  SimpleDataService                                          │
│                                                             │
│           ▼                                                 │
│                                                             │
│  @Bean @Primary                                            │
│  @ConditionalOnProperty("decorator.stack=minimal")         │
│  minimalDecoratedDataService()                             │
│    └─> LoggingDecorator(simpleDataService)                │
│                                                             │
│           OR                                                │
│                                                             │
│  @Bean @Primary                                            │
│  @ConditionalOnProperty("decorator.stack=full")            │
│  fullyDecoratedDataService()                               │
│    └─> FeatureToggleDecorator(                            │
│         EncryptionDecorator(                               │
│           CachingDecorator(                                │
│             LoggingDecorator(simpleDataService))))         │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

## Data Flow Example

### Input: `"Hello World"`

**With Full Stack (Feature Toggle → Encryption → Caching → Logging):**

```
1. Client submits: "Hello World"
                    ↓
2. Feature Toggle: [ENABLED] → passes through
                    ↓
3. Encryption:     "Hello World" → "SGVsbG8gV29ybGQ=" (Base64)
                    ↓
4. Caching:        Stores "SGVsbG8gV29ybGQ=" in cache
                    ↓
5. Logging:        [INFO] Saving data (47 chars encrypted)
                    ↓
6. Base Service:   Stores "SGVsbG8gV29ybGQ=" with ID-1
                    ↓
7. Returns:        ID-1
```

**Retrieval with Cache Hit:**

```
1. Client requests: ID-1
                    ↓
2. Feature Toggle: [ENABLED] → passes through
                    ↓
3. Encryption:     (prepares decryption)
                    ↓
4. Caching:        Cache HIT! → "SGVsbG8gV29ybGQ="
                    ↓
                   (skips Base Service call)
                    ↓
5. Encryption:     "SGVsbG8gV29ybGQ=" → "Hello World" (decrypt)
                    ↓
6. Logging:        [INFO] Retrieved data in 2ms (cache hit)
                    ↓
7. Returns:        "Hello World"
```

## Performance Impact

| Decorator | Overhead | Impact |
|-----------|----------|--------|
| Logging   | ~1-2ms   | Low - mostly I/O for log writing |
| Caching   | ~0.1ms (hit) / ~1ms (miss) | Low - in-memory map lookup |
| Encryption| ~2-5ms   | Medium - depends on data size |
| Feature Toggle | ~0.1ms | Minimal - boolean check |

**Total overhead (full stack):** ~3-8ms per operation
**Cache effectiveness:** Can reduce overhead to <1ms on hits

## Testing Strategy

### Unit Tests
- Test each decorator in isolation
- Mock/stub delegate service
- Verify decorator-specific behavior only

### Integration Tests  
- Test decorator stacking
- Verify data flows correctly through all layers
- Test Spring configuration

### Test Pyramid
```
           ▲
          / \
         /   \        E2E Tests (few)
        /_____\       - Full stack scenarios
       /       \
      /         \     Integration Tests (some)
     /___________\    - Decorator stacking
    /             \   - Spring context
   /               \  
  /_________________\ Unit Tests (many)
                      - Individual decorators
                      - Edge cases
```
