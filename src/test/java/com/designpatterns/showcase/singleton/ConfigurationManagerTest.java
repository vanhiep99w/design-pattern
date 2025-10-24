package com.designpatterns.showcase.singleton;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationManagerTest {

    private ConfigurationManager configManager;

    @BeforeEach
    void setUp() {
        configManager = ConfigurationManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        configManager.clearSettings();
    }

    @Test
    void testGetInstance_ReturnsSameInstance() {
        ConfigurationManager instance1 = ConfigurationManager.getInstance();
        ConfigurationManager instance2 = ConfigurationManager.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    void testGetInstance_MultipleCalls_ReturnsSameHashCode() {
        ConfigurationManager instance1 = ConfigurationManager.getInstance();
        ConfigurationManager instance2 = ConfigurationManager.getInstance();
        ConfigurationManager instance3 = ConfigurationManager.getInstance();

        int hash1 = System.identityHashCode(instance1);
        int hash2 = System.identityHashCode(instance2);
        int hash3 = System.identityHashCode(instance3);

        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
    }

    @Test
    void testDefaultSettings_AreLoaded() {
        assertEquals("Design Pattern Showcase", configManager.getSetting("app.name"));
        assertEquals("1.0.0", configManager.getSetting("app.version"));
        assertEquals("development", configManager.getSetting("app.environment"));
        assertEquals("10", configManager.getSetting("db.pool.size"));
        assertEquals("30000", configManager.getSetting("db.pool.timeout"));
        assertEquals("true", configManager.getSetting("cache.enabled"));
        assertEquals("3600", configManager.getSetting("cache.ttl"));
        assertEquals("INFO", configManager.getSetting("logging.level"));
    }

    @Test
    void testGetSetting_WithDefaultValue() {
        String value = configManager.getSetting("non.existent.key", "default-value");
        assertEquals("default-value", value);
    }

    @Test
    void testSetSetting_UpdatesValue() {
        configManager.setSetting("test.key", "test-value");
        assertEquals("test-value", configManager.getSetting("test.key"));
    }

    @Test
    void testSetSetting_WithNullKey_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                configManager.setSetting(null, "value")
        );
    }

    @Test
    void testSetSetting_WithEmptyKey_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                configManager.setSetting("  ", "value")
        );
    }

    @Test
    void testHasSetting_ExistingKey_ReturnsTrue() {
        assertTrue(configManager.hasSetting("app.name"));
    }

    @Test
    void testHasSetting_NonExistentKey_ReturnsFalse() {
        assertFalse(configManager.hasSetting("non.existent.key"));
    }

    @Test
    void testRemoveSetting() {
        configManager.setSetting("temp.key", "temp-value");
        assertTrue(configManager.hasSetting("temp.key"));

        configManager.removeSetting("temp.key");
        assertFalse(configManager.hasSetting("temp.key"));
    }

    @Test
    void testGetAllSettings_ReturnsImmutableCopy() {
        Map<String, String> settings = configManager.getAllSettings();
        assertNotNull(settings);
        assertTrue(settings.size() >= 8);

        assertThrows(UnsupportedOperationException.class, () ->
                settings.put("new.key", "new-value")
        );
    }

    @Test
    void testClearSettings_ResetsToDefaults() {
        configManager.setSetting("custom.key", "custom-value");
        assertTrue(configManager.hasSetting("custom.key"));

        configManager.clearSettings();
        assertFalse(configManager.hasSetting("custom.key"));
        assertTrue(configManager.hasSetting("app.name"));
    }

    @Test
    void testThreadSafety_MultipleThreadsGetSameInstance() throws InterruptedException {
        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        List<ConfigurationManager> instances = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ConfigurationManager instance = ConfigurationManager.getInstance();
                    synchronized (instances) {
                        instances.add(instance);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(endLatch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(threadCount, instances.size());
        ConfigurationManager firstInstance = instances.get(0);
        for (ConfigurationManager instance : instances) {
            assertSame(firstInstance, instance);
        }
    }

    @Test
    void testThreadSafety_ConcurrentReadsAndWrites() throws InterruptedException {
        int threadCount = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ConfigurationManager manager = ConfigurationManager.getInstance();

                    if (threadId % 2 == 0) {
                        manager.setSetting("thread." + threadId, "value-" + threadId);
                    } else {
                        manager.getSetting("app.name");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(endLatch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        for (int i = 0; i < threadCount; i += 2) {
            assertEquals("value-" + i, configManager.getSetting("thread." + i));
        }
    }
}
