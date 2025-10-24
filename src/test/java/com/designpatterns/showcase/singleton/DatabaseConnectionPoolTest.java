package com.designpatterns.showcase.singleton;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionPoolTest {

    private DatabaseConnectionPool pool;

    @BeforeEach
    void setUp() {
        pool = DatabaseConnectionPool.getInstance();
        if (!pool.isInitialized()) {
            pool.initialize();
        }
    }

    @AfterEach
    void tearDown() {
        pool.shutdown();
    }

    @Test
    void testGetInstance_ReturnsSameInstance() {
        DatabaseConnectionPool instance1 = DatabaseConnectionPool.getInstance();
        DatabaseConnectionPool instance2 = DatabaseConnectionPool.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    void testGetInstance_MultipleCalls_ReturnsSameHashCode() {
        DatabaseConnectionPool instance1 = DatabaseConnectionPool.getInstance();
        DatabaseConnectionPool instance2 = DatabaseConnectionPool.getInstance();
        DatabaseConnectionPool instance3 = DatabaseConnectionPool.getInstance();

        int hash1 = System.identityHashCode(instance1);
        int hash2 = System.identityHashCode(instance2);
        int hash3 = System.identityHashCode(instance3);

        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
    }

    @Test
    void testInitialize_CreatesConnections() {
        pool.shutdown();
        pool = DatabaseConnectionPool.getInstance();

        assertFalse(pool.isInitialized());
        pool.initialize();
        assertTrue(pool.isInitialized());
        assertEquals(10, pool.getAvailableConnectionCount());
        assertEquals(0, pool.getActiveConnectionCount());
    }

    @Test
    void testInitialize_CalledTwice_ThrowsException() {
        assertThrows(IllegalStateException.class, () -> pool.initialize());
    }

    @Test
    void testGetConnection_Success() throws InterruptedException {
        DatabaseConnectionPool.PooledConnection connection = pool.getConnection();

        assertNotNull(connection);
        assertTrue(connection.isInUse());
        assertEquals(9, pool.getAvailableConnectionCount());
        assertEquals(1, pool.getActiveConnectionCount());

        pool.releaseConnection(connection);
    }

    @Test
    void testGetConnection_NotInitialized_ThrowsException() {
        pool.shutdown();
        DatabaseConnectionPool newPool = DatabaseConnectionPool.getInstance();

        assertThrows(IllegalStateException.class, newPool::getConnection);
    }

    @Test
    void testReleaseConnection_Success() throws InterruptedException {
        DatabaseConnectionPool.PooledConnection connection = pool.getConnection();
        assertEquals(9, pool.getAvailableConnectionCount());

        pool.releaseConnection(connection);
        assertEquals(10, pool.getAvailableConnectionCount());
        assertEquals(0, pool.getActiveConnectionCount());
        assertFalse(connection.isInUse());
    }

    @Test
    void testReleaseConnection_NullConnection_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                pool.releaseConnection(null)
        );
    }

    @Test
    void testPooledConnection_ExecuteQuery() throws InterruptedException {
        DatabaseConnectionPool.PooledConnection connection = pool.getConnection();
        long timestampBefore = connection.getLastUsedTimestamp();

        Thread.sleep(10);
        connection.executeQuery("SELECT * FROM users");

        assertTrue(connection.getLastUsedTimestamp() > timestampBefore);
        pool.releaseConnection(connection);
    }

    @Test
    void testPooledConnection_ExecuteQuery_NotInUse_ThrowsException() {
        DatabaseConnectionPool.PooledConnection connection = new DatabaseConnectionPool.PooledConnection("test");

        assertThrows(IllegalStateException.class, () ->
                connection.executeQuery("SELECT * FROM users")
        );
    }

    @Test
    void testGetMaxPoolSize() {
        assertEquals(10, pool.getMaxPoolSize());
    }

    @Test
    void testShutdown_ClearsPool() {
        assertTrue(pool.isInitialized());
        assertEquals(10, pool.getAvailableConnectionCount());

        pool.shutdown();

        assertFalse(pool.isInitialized());
        assertEquals(0, pool.getAvailableConnectionCount());
        assertEquals(0, pool.getActiveConnectionCount());
    }

    @Test
    void testMultipleConnectionsAcquisition() throws InterruptedException {
        List<DatabaseConnectionPool.PooledConnection> connections = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            connections.add(pool.getConnection());
        }

        assertEquals(5, pool.getAvailableConnectionCount());
        assertEquals(5, pool.getActiveConnectionCount());

        for (DatabaseConnectionPool.PooledConnection connection : connections) {
            pool.releaseConnection(connection);
        }

        assertEquals(10, pool.getAvailableConnectionCount());
        assertEquals(0, pool.getActiveConnectionCount());
    }

    @Test
    void testPoolExhaustion_Timeout() {
        List<DatabaseConnectionPool.PooledConnection> connections = new ArrayList<>();

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                connections.add(pool.getConnection());
            }
        });

        assertEquals(0, pool.getAvailableConnectionCount());
        assertEquals(10, pool.getActiveConnectionCount());

        assertThrows(IllegalStateException.class, () ->
                pool.getConnection()
        );

        for (DatabaseConnectionPool.PooledConnection connection : connections) {
            pool.releaseConnection(connection);
        }
    }

    @Test
    void testThreadSafety_MultipleConcurrentAcquisitions() throws InterruptedException {
        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        List<DatabaseConnectionPool.PooledConnection> acquiredConnections = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    DatabaseConnectionPool.PooledConnection conn = pool.getConnection();
                    synchronized (acquiredConnections) {
                        acquiredConnections.add(conn);
                    }
                    Thread.sleep(100);
                    pool.releaseConnection(conn);
                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(endLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertTrue(acquiredConnections.size() + exceptions.size() == threadCount);
        assertTrue(acquiredConnections.size() >= 10);
        assertEquals(10, pool.getAvailableConnectionCount());
        assertEquals(0, pool.getActiveConnectionCount());
    }

    @Test
    void testThreadSafety_DoubleLockingSingleton() throws InterruptedException {
        pool.shutdown();

        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        List<DatabaseConnectionPool> instances = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    DatabaseConnectionPool instance = DatabaseConnectionPool.getInstance();
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
        DatabaseConnectionPool firstInstance = instances.get(0);
        for (DatabaseConnectionPool instance : instances) {
            assertSame(firstInstance, instance);
        }
    }
}
