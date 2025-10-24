package com.designpatterns.showcase.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SimpleDataServiceTest {

    private SimpleDataService dataService;

    @BeforeEach
    void setUp() {
        dataService = new SimpleDataService();
    }

    @Test
    void shouldSaveDataAndReturnId() {
        String data = "Test data";
        String id = dataService.save(data);

        assertNotNull(id);
        assertTrue(id.startsWith("ID-"));
    }

    @Test
    void shouldRetrieveSavedData() {
        String data = "Test data";
        String id = dataService.save(data);

        Optional<String> retrieved = dataService.retrieve(id);

        assertTrue(retrieved.isPresent());
        assertEquals(data, retrieved.get());
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentId() {
        Optional<String> retrieved = dataService.retrieve("non-existent-id");

        assertTrue(retrieved.isEmpty());
    }

    @Test
    void shouldFindAllSavedData() {
        dataService.save("Data 1");
        dataService.save("Data 2");
        dataService.save("Data 3");

        List<String> allData = dataService.findAll();

        assertEquals(3, allData.size());
        assertTrue(allData.contains("Data 1"));
        assertTrue(allData.contains("Data 2"));
        assertTrue(allData.contains("Data 3"));
    }

    @Test
    void shouldDeleteExistingData() {
        String id = dataService.save("Test data");

        boolean deleted = dataService.delete(id);

        assertTrue(deleted);
        assertTrue(dataService.retrieve(id).isEmpty());
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistentData() {
        boolean deleted = dataService.delete("non-existent-id");

        assertFalse(deleted);
    }

    @Test
    void shouldGenerateUniqueIds() {
        String id1 = dataService.save("Data 1");
        String id2 = dataService.save("Data 2");
        String id3 = dataService.save("Data 3");

        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
    }

    @Test
    void clearCacheShouldNotThrowException() {
        assertDoesNotThrow(() -> dataService.clearCache());
    }
}
