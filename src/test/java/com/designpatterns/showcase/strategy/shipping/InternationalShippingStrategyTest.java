package com.designpatterns.showcase.strategy.shipping;

import com.designpatterns.showcase.strategy.dto.ShippingRequest;
import com.designpatterns.showcase.strategy.dto.ShippingCostResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InternationalShippingStrategyTest {

    private InternationalShippingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new InternationalShippingStrategy();
    }

    @Test
    void shouldCalculateInternationalShippingToCanada() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("CANADA")
                .packageWeight(new BigDecimal("2.0"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(new BigDecimal("56.00"), result.getBaseCost());
        assertEquals(BigDecimal.ZERO, result.getAdditionalFees());
        assertEquals(new BigDecimal("56.00"), result.getTotalCost());
        assertEquals("INTERNATIONAL", result.getShippingMethod());
        assertEquals(7, result.getEstimatedDeliveryDays());
        assertTrue(result.getDescription().contains("Customs"));
    }

    @Test
    void shouldCalculateInternationalShippingToUK() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("UK")
                .packageWeight(new BigDecimal("1.5"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(10, result.getEstimatedDeliveryDays());
    }

    @Test
    void shouldAddFragileFee() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("GERMANY")
                .packageWeight(new BigDecimal("1.0"))
                .isFragile(true)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(new BigDecimal("10.00"), result.getAdditionalFees());
        assertTrue(result.getDescription().contains("fragile"));
    }

    @Test
    void shouldAddSignatureFee() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("JAPAN")
                .packageWeight(new BigDecimal("1.0"))
                .isFragile(false)
                .requiresSignature(true)
                .build();

        ShippingCostResult result = strategy.calculateShippingCost(request);

        assertEquals(new BigDecimal("5.00"), result.getAdditionalFees());
        assertEquals(14, result.getEstimatedDeliveryDays());
    }

    @Test
    void shouldSupportInternationalDestinations() {
        assertTrue(strategy.supportsDestination("CANADA"));
        assertTrue(strategy.supportsDestination("UK"));
        assertTrue(strategy.supportsDestination("GERMANY"));
        assertTrue(strategy.supportsDestination("JAPAN"));
        assertTrue(strategy.supportsDestination("AUSTRALIA"));
    }

    @Test
    void shouldNotSupportUSA() {
        assertFalse(strategy.supportsDestination("USA"));
        assertFalse(strategy.supportsDestination("US"));
        assertFalse(strategy.supportsDestination("UNITED STATES"));
    }

    @Test
    void shouldNotSupportNullDestination() {
        assertFalse(strategy.supportsDestination(null));
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("INTERNATIONAL", strategy.getStrategyName());
    }
}
