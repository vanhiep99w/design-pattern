package com.designpatterns.showcase.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionDataServiceDecoratorTest {

    private SimpleDataService baseService;
    private EncryptionDataServiceDecorator encryptionService;
    private static final String ENCRYPTION_KEY = "test-secret-key-12345";

    @BeforeEach
    void setUp() {
        baseService = new SimpleDataService();
        encryptionService = new EncryptionDataServiceDecorator(baseService, ENCRYPTION_KEY);
    }

    @Test
    void shouldEncryptDataBeforeSaving() {
        String plainText = "Sensitive data";
        String id = encryptionService.save(plainText);

        Optional<String> storedData = baseService.retrieve(id);

        assertTrue(storedData.isPresent());
        assertNotEquals(plainText, storedData.get());
        assertTrue(storedData.get().length() > 0);
    }

    @Test
    void shouldDecryptDataAfterRetrieval() {
        String plainText = "Sensitive data";
        String id = encryptionService.save(plainText);

        Optional<String> retrieved = encryptionService.retrieve(id);

        assertTrue(retrieved.isPresent());
        assertEquals(plainText, retrieved.get());
    }

    @Test
    void shouldHandleEmptyData() {
        String plainText = "";
        String id = encryptionService.save(plainText);

        Optional<String> retrieved = encryptionService.retrieve(id);

        assertTrue(retrieved.isPresent());
        assertEquals(plainText, retrieved.get());
    }

    @Test
    void shouldHandleSpecialCharacters() {
        String plainText = "Test!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String id = encryptionService.save(plainText);

        Optional<String> retrieved = encryptionService.retrieve(id);

        assertTrue(retrieved.isPresent());
        assertEquals(plainText, retrieved.get());
    }

    @Test
    void shouldHandleUnicodeCharacters() {
        String plainText = "Hello 世界 🌍 Привет مرحبا";
        String id = encryptionService.save(plainText);

        Optional<String> retrieved = encryptionService.retrieve(id);

        assertTrue(retrieved.isPresent());
        assertEquals(plainText, retrieved.get());
    }

    @Test
    void shouldEncryptAndDecryptMultipleEntries() {
        String data1 = "First sensitive data";
        String data2 = "Second sensitive data";
        String data3 = "Third sensitive data";

        String id1 = encryptionService.save(data1);
        String id2 = encryptionService.save(data2);
        String id3 = encryptionService.save(data3);

        assertEquals(data1, encryptionService.retrieve(id1).orElse(null));
        assertEquals(data2, encryptionService.retrieve(id2).orElse(null));
        assertEquals(data3, encryptionService.retrieve(id3).orElse(null));
    }

    @Test
    void shouldDecryptAllEntries() {
        encryptionService.save("Data 1");
        encryptionService.save("Data 2");
        encryptionService.save("Data 3");

        List<String> allData = encryptionService.findAll();

        assertEquals(3, allData.size());
        assertTrue(allData.contains("Data 1"));
        assertTrue(allData.contains("Data 2"));
        assertTrue(allData.contains("Data 3"));
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentData() {
        Optional<String> retrieved = encryptionService.retrieve("non-existent-id");

        assertTrue(retrieved.isEmpty());
    }

    @Test
    void shouldDeleteEncryptedData() {
        String id = encryptionService.save("Test data");

        boolean deleted = encryptionService.delete(id);

        assertTrue(deleted);
        assertTrue(encryptionService.retrieve(id).isEmpty());
    }

    @Test
    void shouldProduceDifferentEncryptionForSameData() {
        String plainText = "Same data";
        
        String id1 = encryptionService.save(plainText);
        String id2 = encryptionService.save(plainText);

        Optional<String> encrypted1 = baseService.retrieve(id1);
        Optional<String> encrypted2 = baseService.retrieve(id2);

        assertTrue(encrypted1.isPresent());
        assertTrue(encrypted2.isPresent());
        assertEquals(encrypted1.get(), encrypted2.get());
    }

    @Test
    void shouldEncryptLongData() {
        String longData = "A".repeat(1000);
        String id = encryptionService.save(longData);

        Optional<String> retrieved = encryptionService.retrieve(id);

        assertTrue(retrieved.isPresent());
        assertEquals(longData, retrieved.get());
    }

    @Test
    void shouldVerifyEncryptionDecryptionCorrectness() {
        String original = "Test encryption correctness";
        
        String encrypted = encryptionService.encrypt(original);
        String decrypted = encryptionService.decrypt(encrypted);

        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void shouldHandleNullEncryption() {
        String encrypted = encryptionService.encrypt(null);
        assertNull(encrypted);
    }

    @Test
    void shouldHandleNullDecryption() {
        String decrypted = encryptionService.decrypt(null);
        assertNull(decrypted);
    }

    @Test
    void shouldUseDifferentKeyForDifferentDecryptors() {
        EncryptionDataServiceDecorator service1 = new EncryptionDataServiceDecorator(baseService, "key1");
        EncryptionDataServiceDecorator service2 = new EncryptionDataServiceDecorator(baseService, "key2");

        String encrypted1 = service1.encrypt("Test data");
        String encrypted2 = service2.encrypt("Test data");

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void shouldNotDecryptWithWrongKey() {
        String plainText = "Secret message";
        EncryptionDataServiceDecorator service1 = new EncryptionDataServiceDecorator(baseService, "key1");
        EncryptionDataServiceDecorator service2 = new EncryptionDataServiceDecorator(baseService, "key2");

        String encrypted = service1.encrypt(plainText);
        String decrypted = service2.decrypt(encrypted);

        assertNotEquals(plainText, decrypted);
    }
}
