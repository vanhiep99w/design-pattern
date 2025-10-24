package com.designpatterns.showcase.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationCacheService {

    private final Map<String, Object> cache;
    private final long createdAt;

    public ApplicationCacheService() {
        this.cache = new ConcurrentHashMap<>();
        this.createdAt = System.currentTimeMillis();
    }

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public Object get(String key) {
        return cache.get(key);
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> getAll() {
        return Map.copyOf(cache);
    }
}
