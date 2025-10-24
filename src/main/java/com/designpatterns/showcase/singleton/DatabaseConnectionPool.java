package com.designpatterns.showcase.singleton;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnectionPool {

    private static volatile DatabaseConnectionPool instance;
    private final BlockingQueue<PooledConnection> availableConnections;
    private final AtomicInteger activeConnections;
    private final int maxPoolSize;
    private final long connectionTimeout;
    private volatile boolean initialized = false;

    private DatabaseConnectionPool() {
        ConfigurationManager config = ConfigurationManager.getInstance();
        this.maxPoolSize = Integer.parseInt(config.getSetting("db.pool.size", "10"));
        this.connectionTimeout = Long.parseLong(config.getSetting("db.pool.timeout", "30000"));
        this.availableConnections = new LinkedBlockingQueue<>(maxPoolSize);
        this.activeConnections = new AtomicInteger(0);
    }

    public static DatabaseConnectionPool getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionPool();
                }
            }
        }
        return instance;
    }

    public synchronized void initialize() {
        if (initialized) {
            throw new IllegalStateException("Connection pool already initialized");
        }

        for (int i = 0; i < maxPoolSize; i++) {
            availableConnections.offer(new PooledConnection("conn-" + i));
        }
        initialized = true;
    }

    public PooledConnection getConnection() throws InterruptedException {
        if (!initialized) {
            throw new IllegalStateException("Connection pool not initialized");
        }

        PooledConnection connection = availableConnections.poll(connectionTimeout, TimeUnit.MILLISECONDS);
        if (connection == null) {
            throw new IllegalStateException("Connection timeout: No available connections in pool");
        }

        activeConnections.incrementAndGet();
        connection.markInUse();
        return connection;
    }

    public void releaseConnection(PooledConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        connection.markAvailable();
        availableConnections.offer(connection);
        activeConnections.decrementAndGet();
    }

    public int getAvailableConnectionCount() {
        return availableConnections.size();
    }

    public int getActiveConnectionCount() {
        return activeConnections.get();
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public synchronized void shutdown() {
        if (!initialized) {
            return;
        }

        availableConnections.clear();
        activeConnections.set(0);
        initialized = false;
    }

    public static class PooledConnection {
        private final String connectionId;
        private volatile boolean inUse;
        private volatile long lastUsedTimestamp;

        public PooledConnection(String connectionId) {
            this.connectionId = connectionId;
            this.inUse = false;
            this.lastUsedTimestamp = System.currentTimeMillis();
        }

        void markInUse() {
            this.inUse = true;
            this.lastUsedTimestamp = System.currentTimeMillis();
        }

        void markAvailable() {
            this.inUse = false;
        }

        public String getConnectionId() {
            return connectionId;
        }

        public boolean isInUse() {
            return inUse;
        }

        public long getLastUsedTimestamp() {
            return lastUsedTimestamp;
        }

        public void executeQuery(String query) {
            if (!inUse) {
                throw new IllegalStateException("Connection not acquired from pool");
            }
            this.lastUsedTimestamp = System.currentTimeMillis();
        }
    }
}
