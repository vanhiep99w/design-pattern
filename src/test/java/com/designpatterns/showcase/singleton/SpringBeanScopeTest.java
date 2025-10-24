package com.designpatterns.showcase.singleton;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SpringBeanScopeTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ApplicationCacheService singletonCacheService1;

    @Autowired
    private ApplicationCacheService singletonCacheService2;

    @Test
    void testSingletonScope_SameInstance() {
        assertNotNull(singletonCacheService1);
        assertNotNull(singletonCacheService2);
        assertSame(singletonCacheService1, singletonCacheService2);
    }

    @Test
    void testSingletonScope_SameIdentityHashCode() {
        int hash1 = System.identityHashCode(singletonCacheService1);
        int hash2 = System.identityHashCode(singletonCacheService2);
        assertEquals(hash1, hash2);
    }

    @Test
    void testSingletonScope_SharedState() {
        singletonCacheService1.put("key1", "value1");
        assertEquals("value1", singletonCacheService2.get("key1"));

        singletonCacheService2.put("key2", "value2");
        assertEquals("value2", singletonCacheService1.get("key2"));

        assertEquals(singletonCacheService1.size(), singletonCacheService2.size());
    }

    @Test
    void testSingletonScope_CreatedAtSame() {
        assertEquals(singletonCacheService1.getCreatedAt(), singletonCacheService2.getCreatedAt());
    }

    @Test
    void testPrototypeScope_DifferentInstances() {
        SessionService prototypeSession1 = applicationContext.getBean("prototypeSessionService", SessionService.class);
        SessionService prototypeSession2 = applicationContext.getBean("prototypeSessionService", SessionService.class);

        assertNotNull(prototypeSession1);
        assertNotNull(prototypeSession2);
        assertNotSame(prototypeSession1, prototypeSession2);
    }

    @Test
    void testPrototypeScope_DifferentIdentityHashCode() {
        SessionService prototypeSession1 = applicationContext.getBean("prototypeSessionService", SessionService.class);
        SessionService prototypeSession2 = applicationContext.getBean("prototypeSessionService", SessionService.class);

        int hash1 = System.identityHashCode(prototypeSession1);
        int hash2 = System.identityHashCode(prototypeSession2);
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testPrototypeScope_IndependentState() {
        SessionService prototypeSession1 = applicationContext.getBean("prototypeSessionService", SessionService.class);
        SessionService prototypeSession2 = applicationContext.getBean("prototypeSessionService", SessionService.class);

        prototypeSession1.incrementRequestCount();
        prototypeSession1.incrementRequestCount();
        assertEquals(2, prototypeSession1.getRequestCount());

        prototypeSession2.incrementRequestCount();
        assertEquals(1, prototypeSession2.getRequestCount());

        assertNotEquals(prototypeSession1.getRequestCount(), prototypeSession2.getRequestCount());
    }

    @Test
    void testPrototypeScope_DifferentSessionIds() {
        SessionService prototypeSession1 = applicationContext.getBean("prototypeSessionService", SessionService.class);
        SessionService prototypeSession2 = applicationContext.getBean("prototypeSessionService", SessionService.class);

        assertNotEquals(prototypeSession1.getSessionId(), prototypeSession2.getSessionId());
    }

    @Test
    void testPrototypeScope_DifferentCreationTimes() throws InterruptedException {
        SessionService prototypeSession1 = applicationContext.getBean("prototypeSessionService", SessionService.class);
        Thread.sleep(10);
        SessionService prototypeSession2 = applicationContext.getBean("prototypeSessionService", SessionService.class);

        assertTrue(prototypeSession2.getCreatedAt().isAfter(prototypeSession1.getCreatedAt()) ||
                   prototypeSession2.getCreatedAt().isEqual(prototypeSession1.getCreatedAt()));
    }

    @Test
    void testDefaultScope_IsSingleton() {
        SingletonDemoService service1 = applicationContext.getBean(SingletonDemoService.class);
        SingletonDemoService service2 = applicationContext.getBean(SingletonDemoService.class);

        assertSame(service1, service2);
    }

    @Test
    void testSingletonCache_PersistsAcrossMultipleRetrievals() {
        ApplicationCacheService cache1 = applicationContext.getBean("singletonCacheService", ApplicationCacheService.class);
        cache1.put("test-key", "test-value");

        ApplicationCacheService cache2 = applicationContext.getBean("singletonCacheService", ApplicationCacheService.class);
        assertEquals("test-value", cache2.get("test-key"));

        ApplicationCacheService cache3 = applicationContext.getBean("singletonCacheService", ApplicationCacheService.class);
        assertTrue(cache3.contains("test-key"));
    }

    @Test
    void testPrototypeSession_IndependentOperations() {
        SessionService session1 = applicationContext.getBean("prototypeSessionService", SessionService.class);
        SessionService session2 = applicationContext.getBean("prototypeSessionService", SessionService.class);

        String result1 = session1.processRequest("Request A");
        String result2 = session1.processRequest("Request B");
        assertEquals(2, session1.getRequestCount());

        String result3 = session2.processRequest("Request C");
        assertEquals(1, session2.getRequestCount());

        assertNotEquals(result1, result3);
    }

    @Test
    void testMultiplePrototypeBeansCreation() {
        SessionService session1 = applicationContext.getBean("prototypeSessionService", SessionService.class);
        SessionService session2 = applicationContext.getBean("prototypeSessionService", SessionService.class);
        SessionService session3 = applicationContext.getBean("prototypeSessionService", SessionService.class);

        assertNotSame(session1, session2);
        assertNotSame(session2, session3);
        assertNotSame(session1, session3);

        assertNotEquals(session1.getSessionId(), session2.getSessionId());
        assertNotEquals(session2.getSessionId(), session3.getSessionId());
        assertNotEquals(session1.getSessionId(), session3.getSessionId());
    }
}
