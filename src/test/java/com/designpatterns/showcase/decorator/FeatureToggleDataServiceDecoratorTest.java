package com.designpatterns.showcase.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FeatureToggleDataServiceDecoratorTest {

    private SimpleDataService baseService;
    private FeatureToggleDataServiceDecorator featureToggleService;
    private static final String FEATURE_NAME = "test-feature";

    @BeforeEach
    void setUp() {
        baseService = new SimpleDataService();
        featureToggleService = new FeatureToggleDataServiceDecorator(baseService, FEATURE_NAME, true);
    }

    @Test
    void shouldAllowSaveWhenFeatureEnabled() {
        String data = "Test data";
        String id = featureToggleService.save(data);

        assertNotNull(id);
        assertTrue(baseService.retrieve(id).isPresent());
    }

    @Test
    void shouldBlockSaveWhenFeatureDisabled() {
        featureToggleService.disable();

        assertThrows(FeatureDisabledException.class, () -> {
            featureToggleService.save("Test data");
        });
    }

    @Test
    void shouldAllowRetrieveWhenFeatureEnabled() {
        String id = baseService.save("Test data");

        Optional<String> retrieved = featureToggleService.retrieve(id);

        assertTrue(retrieved.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenRetrieveAndFeatureDisabled() {
        String id = baseService.save("Test data");
        featureToggleService.disable();

        Optional<String> retrieved = featureToggleService.retrieve(id);

        assertTrue(retrieved.isEmpty());
    }

    @Test
    void shouldAllowFindAllWhenFeatureEnabled() {
        baseService.save("Data 1");
        baseService.save("Data 2");

        List<String> results = featureToggleService.findAll();

        assertEquals(2, results.size());
    }

    @Test
    void shouldReturnEmptyListWhenFindAllAndFeatureDisabled() {
        baseService.save("Data 1");
        baseService.save("Data 2");
        featureToggleService.disable();

        List<String> results = featureToggleService.findAll();

        assertEquals(0, results.size());
    }

    @Test
    void shouldAllowDeleteWhenFeatureEnabled() {
        String id = baseService.save("Test data");

        boolean deleted = featureToggleService.delete(id);

        assertTrue(deleted);
    }

    @Test
    void shouldReturnFalseWhenDeleteAndFeatureDisabled() {
        String id = baseService.save("Test data");
        featureToggleService.disable();

        boolean deleted = featureToggleService.delete(id);

        assertFalse(deleted);
        assertTrue(baseService.retrieve(id).isPresent());
    }

    @Test
    void shouldAllowClearCacheWhenFeatureEnabled() {
        assertDoesNotThrow(() -> featureToggleService.clearCache());
    }

    @Test
    void shouldNotClearCacheWhenFeatureDisabled() {
        featureToggleService.disable();

        assertDoesNotThrow(() -> featureToggleService.clearCache());
    }

    @Test
    void shouldToggleFeatureOnAndOff() {
        assertTrue(featureToggleService.isEnabled());

        featureToggleService.disable();
        assertFalse(featureToggleService.isEnabled());

        featureToggleService.enable();
        assertTrue(featureToggleService.isEnabled());
    }

    @Test
    void shouldGetFeatureName() {
        assertEquals(FEATURE_NAME, featureToggleService.getFeatureName());
    }

    @Test
    void shouldStartDisabledWhenConfigured() {
        FeatureToggleDataServiceDecorator disabledService = 
            new FeatureToggleDataServiceDecorator(baseService, "disabled-feature", false);

        assertFalse(disabledService.isEnabled());
        assertThrows(FeatureDisabledException.class, () -> {
            disabledService.save("Test data");
        });
    }

    @Test
    void shouldReEnableFeatureAfterDisabling() {
        String id = featureToggleService.save("Test data");
        
        featureToggleService.disable();
        assertTrue(featureToggleService.retrieve(id).isEmpty());

        featureToggleService.enable();
        Optional<String> retrieved = featureToggleService.retrieve(id);
        assertTrue(retrieved.isPresent());
    }

    @Test
    void shouldHandleMultipleEnableCallsGracefully() {
        featureToggleService.enable();
        featureToggleService.enable();
        featureToggleService.enable();

        assertTrue(featureToggleService.isEnabled());
        assertDoesNotThrow(() -> featureToggleService.save("Test data"));
    }

    @Test
    void shouldHandleMultipleDisableCallsGracefully() {
        featureToggleService.disable();
        featureToggleService.disable();
        featureToggleService.disable();

        assertFalse(featureToggleService.isEnabled());
        assertThrows(FeatureDisabledException.class, () -> {
            featureToggleService.save("Test data");
        });
    }

    @Test
    void shouldThrowExceptionWithCorrectMessage() {
        featureToggleService.disable();

        FeatureDisabledException exception = assertThrows(FeatureDisabledException.class, () -> {
            featureToggleService.save("Test data");
        });

        assertTrue(exception.getMessage().contains(FEATURE_NAME));
        assertTrue(exception.getMessage().contains("disabled"));
    }
}
