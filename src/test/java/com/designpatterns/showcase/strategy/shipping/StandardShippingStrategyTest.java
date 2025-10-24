package com.designpatterns.showcase.strategy.shipping;

import com.designpatterns.showcase.strategy.dto.ShippingRequest;
import com.designpatterns.showcase.strategy.dto.ShippingCostResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StandardShippingStrategyTest {

    private StandardShippingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StandardShippingStrategy();
    }

    @Test
    void shouldCalculateBasicShippingCost() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("2.5"))
                .packageVolume(new BigDecimal("0.5"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(new BigDecimal("10.00"), result.getBaseCost());
        assertEquals(BigDecimal.ZERO, result.getAdditionalFees());
        assertEquals(new BigDecimal("10.00"), result.getTotalCost());
        assertEquals("STANDARD", result.getShippingMethod());
        assertEquals(5, result.getEstimatedDeliveryDays());
    }

    @Test
    void shouldAddFragileFee() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("US")
                .packageWeight(new BigDecimal("1.0"))
                .isFragile(true)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(new BigDecimal("3.00"), result.getAdditionalFees());
        assertEquals(new BigDecimal("10.00"), result.getTotalCost());
        assertTrue(result.getDescription().contains("Fragile"));
    }

    @Test
    void shouldAddSignatureFee() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("UNITED STATES")
                .packageWeight(new BigDecimal("1.0"))
                .isFragile(false)
                .requiresSignature(true)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(new BigDecimal("2.00"), result.getAdditionalFees());
        assertTrue(result.getDescription().contains("Signature"));
    }

    @Test
    void shouldAddBothFragileAndSignatureFees() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("3.0"))
                .isFragile(true)
                .requiresSignature(true)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(new BigDecimal("5.00"), result.getAdditionalFees());
        assertEquals(new BigDecimal("16.00"), result.getTotalCost());
    }

    @Test
    void shouldSupportUSA() {
        assertTrue(strategy.supportsDestination("USA"));
        assertTrue(strategy.supportsDestination("US"));
        assertTrue(strategy.supportsDestination("UNITED STATES"));
        assertTrue(strategy.supportsDestination("usa"));
    }

    @Test
    void shouldNotSupportInternationalDestinations() {
        assertFalse(strategy.supportsDestination("CANADA"));
        assertFalse(strategy.supportsDestination("UK"));
        assertFalse(strategy.supportsDestination("GERMANY"));
    }

    @Test
    void shouldNotSupportNullDestination() {
        assertFalse(strategy.supportsDestination(null));
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("STANDARD", strategy.getStrategyName());
    }
}
