package com.designpatterns.showcase.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SingletonDemoService {

    private static final Logger logger = LoggerFactory.getLogger(SingletonDemoService.class);

    public void demonstrateClassicSingletons() {
        logger.info("=== Demonstrating Classic Singleton Patterns ===");

        ConfigurationManager configManager1 = ConfigurationManager.getInstance();
        ConfigurationManager configManager2 = ConfigurationManager.getInstance();

        logger.info("ConfigurationManager instance 1: {}", System.identityHashCode(configManager1));
        logger.info("ConfigurationManager instance 2: {}", System.identityHashCode(configManager2));
        logger.info("Both instances are same: {}", configManager1 == configManager2);

        logger.info("App Name: {}", configManager1.getSetting("app.name"));
        logger.info("App Version: {}", configManager1.getSetting("app.version"));

        DatabaseConnectionPool pool1 = DatabaseConnectionPool.getInstance();
        DatabaseConnectionPool pool2 = DatabaseConnectionPool.getInstance();

        logger.info("DatabaseConnectionPool instance 1: {}", System.identityHashCode(pool1));
        logger.info("DatabaseConnectionPool instance 2: {}", System.identityHashCode(pool2));
        logger.info("Both instances are same: {}", pool1 == pool2);

        pool1.initialize();
        logger.info("Connection pool initialized with max size: {}", pool1.getMaxPoolSize());
        logger.info("Available connections: {}", pool1.getAvailableConnectionCount());

        try {
            DatabaseConnectionPool.PooledConnection conn = pool1.getConnection();
            logger.info("Acquired connection: {}", conn.getConnectionId());
            logger.info("Active connections: {}", pool1.getActiveConnectionCount());
            pool1.releaseConnection(conn);
            logger.info("Released connection, available: {}", pool1.getAvailableConnectionCount());
        } catch (InterruptedException e) {
            logger.error("Error acquiring connection", e);
            Thread.currentThread().interrupt();
        } finally {
            pool1.shutdown();
        }
    }

    public String getConfigurationSetting(String key) {
        return ConfigurationManager.getInstance().getSetting(key);
    }

    public void updateConfigurationSetting(String key, String value) {
        ConfigurationManager.getInstance().setSetting(key, value);
    }
}
