package com.designpatterns.showcase.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationManager {

    private final Map<String, String> settings;

    private ConfigurationManager() {
        settings = new ConcurrentHashMap<>();
        loadDefaultSettings();
    }

    public static ConfigurationManager getInstance() {
        return Holder.INSTANCE;
    }

    private void loadDefaultSettings() {
        settings.put("app.name", "Design Pattern Showcase");
        settings.put("app.version", "1.0.0");
        settings.put("app.environment", "development");
        settings.put("db.pool.size", "10");
        settings.put("db.pool.timeout", "30000");
        settings.put("cache.enabled", "true");
        settings.put("cache.ttl", "3600");
        settings.put("logging.level", "INFO");
    }

    public String getSetting(String key) {
        return settings.get(key);
    }

    public String getSetting(String key, String defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    public void setSetting(String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Setting key cannot be null or empty");
        }
        settings.put(key, value);
    }

    public Map<String, String> getAllSettings() {
        return Map.copyOf(settings);
    }

    public boolean hasSetting(String key) {
        return settings.containsKey(key);
    }

    public void removeSetting(String key) {
        settings.remove(key);
    }

    public void clearSettings() {
        settings.clear();
        loadDefaultSettings();
    }

    private static class Holder {
        private static final ConfigurationManager INSTANCE = new ConfigurationManager();
    }
}
