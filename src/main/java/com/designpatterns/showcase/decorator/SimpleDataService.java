package com.designpatterns.showcase.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component("simpleDataService")
public class SimpleDataService implements DataService {
    private static final Logger logger = LoggerFactory.getLogger(SimpleDataService.class);
    private final Map<String, String> dataStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public String save(String data) {
        String id = "ID-" + idGenerator.getAndIncrement();
        dataStore.put(id, data);
        logger.debug("Data saved with id: {}", id);
        return id;
    }

    @Override
    public Optional<String> retrieve(String id) {
        String data = dataStore.get(id);
        logger.debug("Retrieved data for id: {}", id);
        return Optional.ofNullable(data);
    }

    @Override
    public List<String> findAll() {
        logger.debug("Finding all data entries");
        return new ArrayList<>(dataStore.values());
    }

    @Override
    public boolean delete(String id) {
        boolean removed = dataStore.remove(id) != null;
        logger.debug("Deleted data with id: {}, success: {}", id, removed);
        return removed;
    }

    @Override
    public void clearCache() {
        logger.debug("No cache to clear in SimpleDataService");
    }
}
