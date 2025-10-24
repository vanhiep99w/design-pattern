package com.designpatterns.showcase.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CachingDataServiceDecoratorTest {

    private SimpleDataService baseService;
    private CachingDataServiceDecorator cachingService;

    @BeforeEach
    void setUp() {
        baseService = new SimpleDataService();
        cachingService = new CachingDataServiceDecorator(baseService, 5000);
    }

    @Test
    void shouldCacheDataAfterSave() {
        String data = "Test data";
        String id = cachingService.save(data);

        assertEquals(1, cachingService.getCacheSize());

        Optional<String> retrieved = cachingService.retrieve(id);

        assertTrue(retrieved.isPresent());
        assertEquals(data, retrieved.get());
        assertEquals(1, cachingService.getCacheHits());
        assertEquals(0, cachingService.getCacheMisses());
    }

    @Test
    void shouldCacheDataAfterRetrieve() {
        String id = baseService.save("Test data");

        cachingService.retrieve(id);
        assertEquals(1, cachingService.getCacheMisses());
        assertEquals(1, cachingService.getCacheSize());

        cachingService.retrieve(id);
        assertEquals(1, cachingService.getCacheHits());
    }

    @Test
    void shouldRecordCacheMissWhenDataNotInCache() {
        cachingService.retrieve("non-existent-id");

        assertEquals(0, cachingService.getCacheHits());
        assertEquals(1, cachingService.getCacheMisses());
    }

    @Test
    void shouldEvictCacheEntryOnDelete() {
        String id = cachingService.save("Test data");
        assertEquals(1, cachingService.getCacheSize());

        cachingService.delete(id);

        assertEquals(0, cachingService.getCacheSize());
    }

    @Test
    void shouldClearAllCacheEntries() {
        cachingService.save("Data 1");
        cachingService.save("Data 2");
        cachingService.save("Data 3");

        assertEquals(3, cachingService.getCacheSize());

        cachingService.clearCache();

        assertEquals(0, cachingService.getCacheSize());
        assertEquals(0, cachingService.getCacheHits());
        assertEquals(0, cachingService.getCacheMisses());
    }

    @Test
    void shouldCalculateCacheHitRate() {
        String id = cachingService.save("Test data");

        cachingService.retrieve(id);
        cachingService.retrieve(id);
        cachingService.retrieve("non-existent-id");

        assertEquals(2, cachingService.getCacheHits());
        assertEquals(1, cachingService.getCacheMisses());
        assertEquals(2.0 / 3.0, cachingService.getCacheHitRate(), 0.001);
    }

    @Test
    void shouldReturnZeroHitRateWhenNoOperations() {
        assertEquals(0.0, cachingService.getCacheHitRate());
    }

    @Test
    void shouldExpireCacheEntriesAfterTTL() throws InterruptedException {
        CachingDataServiceDecorator shortTtlCache = new CachingDataServiceDecorator(baseService, 100);
        String id = shortTtlCache.save("Test data");

        Optional<String> retrieved1 = shortTtlCache.retrieve(id);
        assertTrue(retrieved1.isPresent());
        assertEquals(1, shortTtlCache.getCacheHits());

        Thread.sleep(150);

        Optional<String> retrieved2 = shortTtlCache.retrieve(id);
        assertTrue(retrieved2.isPresent());
        assertEquals(1, shortTtlCache.getCacheHits());
        assertEquals(1, shortTtlCache.getCacheMisses());
    }

    @Test
    void shouldNotCacheFindAllResults() {
        cachingService.save("Data 1");
        cachingService.save("Data 2");

        List<String> results = cachingService.findAll();

        assertEquals(2, results.size());
        assertEquals(2, cachingService.getCacheSize());
    }

    @Test
    void shouldHandleMultipleConcurrentRetrievals() {
        String id = cachingService.save("Test data");

        for (int i = 0; i < 10; i++) {
            Optional<String> retrieved = cachingService.retrieve(id);
            assertTrue(retrieved.isPresent());
        }

        assertEquals(10, cachingService.getCacheHits());
        assertEquals(0, cachingService.getCacheMisses());
    }

    @Test
    void shouldDelegateToBaseService() {
        String data = "Test data";
        String id = cachingService.save(data);

        Optional<String> retrieved = baseService.retrieve(id);

        assertTrue(retrieved.isPresent());
        assertEquals(data, retrieved.get());
    }

    @Test
    void shouldUseCacheEffectively() {
        String id1 = cachingService.save("Data 1");
        String id2 = cachingService.save("Data 2");

        for (int i = 0; i < 5; i++) {
            cachingService.retrieve(id1);
            cachingService.retrieve(id2);
        }

        assertEquals(10, cachingService.getCacheHits());
        assertEquals(0, cachingService.getCacheMisses());
        assertEquals(1.0, cachingService.getCacheHitRate());
    }
}
