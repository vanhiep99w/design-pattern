package com.designpatterns.showcase.singleton;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SingletonIntegrationTest {

    @Autowired
    private SingletonDemoService singletonDemoService;

    @Autowired
    private ApplicationCacheService cacheService;

    @Test
    void testSingletonDemoServiceIsInjected() {
        assertNotNull(singletonDemoService);
    }

    @Test
    void testApplicationCacheServiceIsInjected() {
        assertNotNull(cacheService);
    }

    @Test
    void testSingletonDemoService_GetConfigurationSetting() {
        String appName = singletonDemoService.getConfigurationSetting("app.name");
        assertEquals("Design Pattern Showcase", appName);
    }

    @Test
    void testSingletonDemoService_UpdateConfigurationSetting() {
        singletonDemoService.updateConfigurationSetting("test.integration", "integration-value");
        String value = singletonDemoService.getConfigurationSetting("test.integration");
        assertEquals("integration-value", value);
    }

    @Test
    void testSingletonDemoService_DemonstrateClassicSingletons() {
        assertDoesNotThrow(() -> singletonDemoService.demonstrateClassicSingletons());
    }

    @Test
    void testApplicationCacheService_BasicOperations() {
        cacheService.put("integration-key", "integration-value");
        assertTrue(cacheService.contains("integration-key"));
        assertEquals("integration-value", cacheService.get("integration-key"));

        cacheService.remove("integration-key");
        assertFalse(cacheService.contains("integration-key"));
    }

    @Test
    void testClassicSingletons_WorkOutsideSpringContext() {
        ConfigurationManager config1 = ConfigurationManager.getInstance();
        ConfigurationManager config2 = ConfigurationManager.getInstance();

        assertSame(config1, config2);
        assertNotNull(config1.getSetting("app.name"));
    }

    @Test
    void testDatabaseConnectionPool_WorkOutsideSpringContext() {
        DatabaseConnectionPool pool1 = DatabaseConnectionPool.getInstance();
        DatabaseConnectionPool pool2 = DatabaseConnectionPool.getInstance();

        assertSame(pool1, pool2);
    }
}
