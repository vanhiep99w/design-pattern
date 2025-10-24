package com.designpatterns.showcase.decorator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LoggingDataServiceDecoratorTest {

    private SimpleDataService baseService;
    private LoggingDataServiceDecorator loggingService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        baseService = new SimpleDataService();
        loggingService = new LoggingDataServiceDecorator(baseService);

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
    void shouldLogSaveOperation() {
        String data = "Test data";
        loggingService.save(data);

        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Attempting to save data") && 
            event.getLevel() == Level.INFO));
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Successfully saved data with ID") && 
            event.getLevel() == Level.INFO));
    }

    @Test
    void shouldLogRetrieveOperation() {
        String id = loggingService.save("Test data");
        listAppender.list.clear();

        loggingService.retrieve(id);

        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Retrieving data for ID") && 
            event.getLevel() == Level.INFO));
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Successfully retrieved data for ID") && 
            event.getLevel() == Level.INFO));
    }

    @Test
    void shouldLogWhenDataNotFound() {
        loggingService.retrieve("non-existent-id");

        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("No data found for ID") && 
            event.getLevel() == Level.INFO));
    }

    @Test
    void shouldLogFindAllOperation() {
        loggingService.save("Data 1");
        loggingService.save("Data 2");
        listAppender.list.clear();

        loggingService.findAll();

        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Finding all data entries") && 
            event.getLevel() == Level.INFO));
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Found 2 entries") && 
            event.getLevel() == Level.INFO));
    }

    @Test
    void shouldLogDeleteOperation() {
        String id = loggingService.save("Test data");
        listAppender.list.clear();

        loggingService.delete(id);

        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Attempting to delete data with ID") && 
            event.getLevel() == Level.INFO));
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Delete operation for ID") && 
            event.getLevel() == Level.INFO));
    }

    @Test
    void shouldLogClearCacheOperation() {
        loggingService.clearCache();

        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Clearing cache") && 
            event.getLevel() == Level.INFO));
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getMessage().contains("Cache cleared") && 
            event.getLevel() == Level.INFO));
    }

    @Test
    void shouldDelegateToBaseService() {
        String data = "Test data";
        String id = loggingService.save(data);

        Optional<String> retrieved = loggingService.retrieve(id);

        assertTrue(retrieved.isPresent());
        assertEquals(data, retrieved.get());
    }

    @Test
    void shouldTruncateLongDataInLogs() {
        String longData = "A".repeat(100);
        loggingService.save(longData);

        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getFormattedMessage().contains("...") && 
            !event.getFormattedMessage().contains(longData)));
    }

    @Test
    void shouldLogExecutionTime() {
        loggingService.save("Test data");

        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream().anyMatch(event -> 
            event.getFormattedMessage().matches(".*in \\d+ms.*")));
    }
}
