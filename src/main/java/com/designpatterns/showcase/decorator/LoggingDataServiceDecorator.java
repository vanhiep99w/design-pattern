package com.designpatterns.showcase.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class LoggingDataServiceDecorator extends DataServiceDecorator {
    private static final Logger logger = LoggerFactory.getLogger(LoggingDataServiceDecorator.class);

    public LoggingDataServiceDecorator(DataService delegate) {
        super(delegate);
    }

    @Override
    public String save(String data) {
        logger.info("[LOGGING] Attempting to save data: {}", truncate(data));
        long startTime = System.currentTimeMillis();
        try {
            String id = delegate.save(data);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[LOGGING] Successfully saved data with ID: {} in {}ms", id, duration);
            return id;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[LOGGING] Failed to save data after {}ms: {}", duration, e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<String> retrieve(String id) {
        logger.info("[LOGGING] Retrieving data for ID: {}", id);
        long startTime = System.currentTimeMillis();
        try {
            Optional<String> result = delegate.retrieve(id);
            long duration = System.currentTimeMillis() - startTime;
            if (result.isPresent()) {
                logger.info("[LOGGING] Successfully retrieved data for ID: {} in {}ms", id, duration);
            } else {
                logger.info("[LOGGING] No data found for ID: {} in {}ms", id, duration);
            }
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[LOGGING] Failed to retrieve data for ID: {} after {}ms: {}", id, duration, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<String> findAll() {
        logger.info("[LOGGING] Finding all data entries");
        long startTime = System.currentTimeMillis();
        try {
            List<String> result = delegate.findAll();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[LOGGING] Found {} entries in {}ms", result.size(), duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[LOGGING] Failed to find all entries after {}ms: {}", duration, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String id) {
        logger.info("[LOGGING] Attempting to delete data with ID: {}", id);
        long startTime = System.currentTimeMillis();
        try {
            boolean result = delegate.delete(id);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[LOGGING] Delete operation for ID: {} completed with result: {} in {}ms", id, result, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[LOGGING] Failed to delete data with ID: {} after {}ms: {}", id, duration, e.getMessage());
            throw e;
        }
    }

    @Override
    public void clearCache() {
        logger.info("[LOGGING] Clearing cache");
        delegate.clearCache();
        logger.info("[LOGGING] Cache cleared");
    }

    private String truncate(String data) {
        if (data == null) {
            return "null";
        }
        return data.length() > 50 ? data.substring(0, 47) + "..." : data;
    }
}
