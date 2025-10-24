package com.designpatterns.showcase.decorator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DecoratorIntegrationTest {

    @Autowired
    private DataService dataService;

    @Test
    void shouldInjectDataService() {
        assertNotNull(dataService);
    }

    @Test
    void shouldSaveAndRetrieveData() {
        String data = "Integration test data";
        String id = dataService.save(data);

        assertNotNull(id);
        Optional<String> retrieved = dataService.retrieve(id);
        
        assertTrue(retrieved.isPresent());
        assertEquals(data, retrieved.get());
    }

    @Test
    void shouldHandleMultipleOperations() {
        String id1 = dataService.save("Data 1");
        String id2 = dataService.save("Data 2");
        String id3 = dataService.save("Data 3");

        assertEquals("Data 1", dataService.retrieve(id1).orElse(null));
        assertEquals("Data 2", dataService.retrieve(id2).orElse(null));
        assertEquals("Data 3", dataService.retrieve(id3).orElse(null));

        assertTrue(dataService.delete(id2));
        assertFalse(dataService.retrieve(id2).isPresent());
    }

    @Test
    void shouldClearCache() {
        assertDoesNotThrow(() -> dataService.clearCache());
    }
}
