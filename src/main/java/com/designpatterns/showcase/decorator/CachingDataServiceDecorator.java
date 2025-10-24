package com.designpatterns.showcase.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CachingDataServiceDecorator extends DataServiceDecorator {
    private static final Logger logger = LoggerFactory.getLogger(CachingDataServiceDecorator.class);
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private int cacheHits = 0;
    private int cacheMisses = 0;

    public CachingDataServiceDecorator(DataService delegate, long ttlMillis) {
        super(delegate);
        this.ttlMillis = ttlMillis;
    }

    public CachingDataServiceDecorator(DataService delegate) {
        this(delegate, 60000);
    }

    @Override
    public String save(String data) {
        String id = delegate.save(data);
        cache.put(id, new CacheEntry(data, Instant.now()));
        logger.debug("Cached data for ID: {}", id);
        return id;
    }

    @Override
    public Optional<String> retrieve(String id) {
        CacheEntry entry = cache.get(id);
        if (entry != null && !entry.isExpired(ttlMillis)) {
            cacheHits++;
            logger.debug("Cache hit for ID: {} (hits: {}, misses: {})", id, cacheHits, cacheMisses);
            return Optional.of(entry.data);
        }

        cacheMisses++;
        logger.debug("Cache miss for ID: {} (hits: {}, misses: {})", id, cacheHits, cacheMisses);
        Optional<String> result = delegate.retrieve(id);
        result.ifPresent(data -> cache.put(id, new CacheEntry(data, Instant.now())));
        return result;
    }

    @Override
    public List<String> findAll() {
        return delegate.findAll();
    }

    @Override
    public boolean delete(String id) {
        cache.remove(id);
        logger.debug("Evicted cache entry for ID: {}", id);
        return delegate.delete(id);
    }

    @Override
    public void clearCache() {
        int size = cache.size();
        cache.clear();
        cacheHits = 0;
        cacheMisses = 0;
        logger.info("Cleared {} cache entries and reset statistics", size);
        delegate.clearCache();
    }

    public int getCacheSize() {
        return cache.size();
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public int getCacheMisses() {
        return cacheMisses;
    }

    public double getCacheHitRate() {
        int total = cacheHits + cacheMisses;
        return total == 0 ? 0.0 : (double) cacheHits / total;
    }

    private static class CacheEntry {
        private final String data;
        private final Instant timestamp;

        CacheEntry(String data, Instant timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }

        boolean isExpired(long ttlMillis) {
            return Instant.now().toEpochMilli() - timestamp.toEpochMilli() > ttlMillis;
        }
    }
}
