package com.designpatterns.showcase.decorator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DecoratorStackingTest {

    private SimpleDataService baseService;
    private DataService fullyDecoratedService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        baseService = new SimpleDataService();
        
        DataService service = baseService;
        service = new LoggingDataServiceDecorator(service);
        service = new CachingDataServiceDecorator(service, 5000);
        service = new EncryptionDataServiceDecorator(service, "test-key");
        service = new FeatureToggleDataServiceDecorator(service, "full-stack", true);
        
        fullyDecoratedService = service;

        logger = (Logger) LoggerFactory.getLogger(LoggingDataServiceDecorator.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldApplyAllDecoratorsInOrder() {
        String plainText = "Test data";
        String id = fullyDecoratedService.save(plainText);

        Optional<String> storedInBase = baseService.retrieve(id);
        assertTrue(storedInBase.isPresent());
        assertNotEquals(plainText, storedInBase.get());

        Optional<String> retrieved = fullyDecoratedService.retrieve(id);
        assertTrue(retrieved.isPresent());
        assertEquals(plainText, retrieved.get());
    }

    @Test
    void shouldLogWhenFullyDecorated() {
        fullyDecoratedService.save("Test data");

        assertTrue(listAppender.list.stream().anyMatch(event -> 
            event.getMessage().contains("Attempting to save data") && 
            event.getLevel() == Level.INFO));
    }

    @Test
    void shouldCacheEncryptedData() {
        CachingDataServiceDecorator cachingLayer = findCachingLayer(fullyDecoratedService);
        assertNotNull(cachingLayer, "Caching layer should be present in the stack");

        String id = fullyDecoratedService.save("Test data");
        
        fullyDecoratedService.retrieve(id);
        fullyDecoratedService.retrieve(id);

        assertEquals(2, cachingLayer.getCacheHits());
    }

    @Test
    void shouldEncryptBeforeCaching() {
        String plainText = "Sensitive data";
        String id = fullyDecoratedService.save(plainText);

        Optional<String> storedData = baseService.retrieve(id);
        assertTrue(storedData.isPresent());
        assertNotEquals(plainText, storedData.get());
    }

    @Test
    void shouldBlockAllOperationsWhenFeatureToggleDisabled() {
        FeatureToggleDataServiceDecorator featureToggle = findFeatureToggleLayer(fullyDecoratedService);
        assertNotNull(featureToggle, "Feature toggle layer should be present in the stack");

        featureToggle.disable();

        assertThrows(FeatureDisabledException.class, () -> {
            fullyDecoratedService.save("Test data");
        });
    }

    @Test
    void shouldClearCacheThroughAllLayers() {
        CachingDataServiceDecorator cachingLayer = findCachingLayer(fullyDecoratedService);
        assertNotNull(cachingLayer);

        fullyDecoratedService.save("Data 1");
        fullyDecoratedService.save("Data 2");
        
        int sizeBefore = cachingLayer.getCacheSize();
        assertTrue(sizeBefore > 0);

        fullyDecoratedService.clearCache();

        assertEquals(0, cachingLayer.getCacheSize());
    }

    @Test
    void shouldMaintainDataIntegrityThroughAllLayers() {
        String data1 = "First data";
        String data2 = "Second data with special chars: !@#$%";
        String data3 = "Third data 世界";

        String id1 = fullyDecoratedService.save(data1);
        String id2 = fullyDecoratedService.save(data2);
        String id3 = fullyDecoratedService.save(data3);

        assertEquals(data1, fullyDecoratedService.retrieve(id1).orElse(null));
        assertEquals(data2, fullyDecoratedService.retrieve(id2).orElse(null));
        assertEquals(data3, fullyDecoratedService.retrieve(id3).orElse(null));
    }

    @Test
    void shouldBenefitFromCachingOnSecondRetrieval() {
        CachingDataServiceDecorator cachingLayer = findCachingLayer(fullyDecoratedService);
        assertNotNull(cachingLayer);

        String id = fullyDecoratedService.save("Test data");

        fullyDecoratedService.retrieve(id);
        int missesAfterFirst = cachingLayer.getCacheMisses();
        
        fullyDecoratedService.retrieve(id);
        int hitsAfterSecond = cachingLayer.getCacheHits();

        assertEquals(0, missesAfterFirst);
        assertEquals(2, hitsAfterSecond);
    }

    @Test
    void shouldHandleDeleteThroughAllLayers() {
        CachingDataServiceDecorator cachingLayer = findCachingLayer(fullyDecoratedService);
        assertNotNull(cachingLayer);

        String id = fullyDecoratedService.save("Test data");
        assertEquals(1, cachingLayer.getCacheSize());

        boolean deleted = fullyDecoratedService.delete(id);

        assertTrue(deleted);
        assertEquals(0, cachingLayer.getCacheSize());
        assertTrue(baseService.retrieve(id).isEmpty());
    }

    @Test
    void shouldStackDecoratorsWithDifferentOrder() {
        DataService service1 = baseService;
        service1 = new EncryptionDataServiceDecorator(service1, "key1");
        service1 = new CachingDataServiceDecorator(service1, 5000);
        service1 = new LoggingDataServiceDecorator(service1);

        DataService service2 = baseService;
        service2 = new CachingDataServiceDecorator(service2, 5000);
        service2 = new EncryptionDataServiceDecorator(service2, "key1");
        service2 = new LoggingDataServiceDecorator(service2);

        String plainText = "Test order";
        String id1 = service1.save(plainText);
        String id2 = service2.save(plainText);

        assertEquals(plainText, service1.retrieve(id1).orElse(null));
        assertEquals(plainText, service2.retrieve(id2).orElse(null));
    }

    private CachingDataServiceDecorator findCachingLayer(DataService service) {
        if (service instanceof CachingDataServiceDecorator) {
            return (CachingDataServiceDecorator) service;
        }
        if (service instanceof DataServiceDecorator decorator) {
            return findCachingLayer(decorator.delegate);
        }
        return null;
    }

    private FeatureToggleDataServiceDecorator findFeatureToggleLayer(DataService service) {
        if (service instanceof FeatureToggleDataServiceDecorator) {
            return (FeatureToggleDataServiceDecorator) service;
        }
        if (service instanceof DataServiceDecorator decorator) {
            return findFeatureToggleLayer(decorator.delegate);
        }
        return null;
    }
}
