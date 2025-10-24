package com.designpatterns.showcase.strategy.context;

import com.designpatterns.showcase.strategy.dto.ShippingRequest;
import com.designpatterns.showcase.strategy.dto.ShippingCostResult;
import com.designpatterns.showcase.strategy.exception.UnsupportedStrategyException;
import com.designpatterns.showcase.strategy.shipping.ExpressShippingStrategy;
import com.designpatterns.showcase.strategy.shipping.InternationalShippingStrategy;
import com.designpatterns.showcase.strategy.shipping.StandardShippingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ShippingContextTest {

    private ShippingContext context;

    @BeforeEach
    void setUp() {
        context = new ShippingContext(
                new StandardShippingStrategy(),
                new ExpressShippingStrategy(),
                new InternationalShippingStrategy()
        );
    }

    @Test
    void shouldCalculateStandardShipping() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("2.0"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = context.calculateShippingCost("STANDARD", request);

        assertEquals("STANDARD", result.getShippingMethod());
        assertEquals(5, result.getEstimatedDeliveryDays());
    }

    @Test
    void shouldCalculateExpressShipping() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("2.0"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = context.calculateShippingCost("EXPRESS", request);

        assertEquals("EXPRESS", result.getShippingMethod());
        assertEquals(2, result.getEstimatedDeliveryDays());
    }

    @Test
    void shouldCalculateInternationalShipping() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("CANADA")
                .packageWeight(new BigDecimal("2.0"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = context.calculateShippingCost("INTERNATIONAL", request);

        assertEquals("INTERNATIONAL", result.getShippingMethod());
        assertTrue(result.getEstimatedDeliveryDays() >= 7);
    }

    @Test
    void shouldThrowExceptionForUnknownStrategy() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("2.0"))
                .build();

        assertThrows(UnsupportedStrategyException.class, () -> {
            context.calculateShippingCost("UNKNOWN", request);
        });
    }

    @Test
    void shouldThrowExceptionWhenStrategyDoesNotSupportDestination() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("CANADA")
                .packageWeight(new BigDecimal("2.0"))
                .build();

        assertThrows(UnsupportedStrategyException.class, () -> {
            context.calculateShippingCost("STANDARD", request);
        });
    }

    @Test
    void shouldAutoSelectStandardForUSA() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("2.0"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = context.calculateShippingCostAuto(request);

        assertEquals("STANDARD", result.getShippingMethod());
    }

    @Test
    void shouldAutoSelectInternationalForCanada() {
        ShippingRequest request = ShippingRequest.builder()
                .destinationCountry("CANADA")
                .packageWeight(new BigDecimal("2.0"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        ShippingCostResult result = context.calculateShippingCostAuto(request);

        assertEquals("INTERNATIONAL", result.getShippingMethod());
    }
}
