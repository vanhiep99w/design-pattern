package com.designpatterns.showcase.strategy.shipping;

import com.designpatterns.showcase.strategy.dto.ShippingRequest;
import com.designpatterns.showcase.strategy.dto.ShippingCostResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExpressShippingStrategyTest {

    private ExpressShippingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ExpressShippingStrategy();
    }

    @Test
    void shouldCalculateExpressShippingCost() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("2.0"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(new BigDecimal("35.00"), result.getBaseCost());
        assertEquals(BigDecimal.ZERO, result.getAdditionalFees());
        assertEquals(new BigDecimal("35.00"), result.getTotalCost());
        assertEquals("EXPRESS", result.getShippingMethod());
        assertEquals(2, result.getEstimatedDeliveryDays());
        assertTrue(result.getDescription().contains("Priority handling"));
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

        assertEquals(new BigDecimal("5.00"), result.getAdditionalFees());
        assertTrue(result.getDescription().contains("Fragile"));
    }

    @Test
    void shouldAddSignatureFee() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("1.0"))
                .isFragile(false)
                .requiresSignature(true)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(new BigDecimal("3.00"), result.getAdditionalFees());
    }

    @Test
    void shouldSupportUSA() {
        assertTrue(strategy.supportsDestination("USA"));
        assertTrue(strategy.supportsDestination("US"));
        assertTrue(strategy.supportsDestination("united states"));
    }

    @Test
    void shouldNotSupportInternationalDestinations() {
        assertFalse(strategy.supportsDestination("CANADA"));
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("EXPRESS", strategy.getStrategyName());
    }
}
