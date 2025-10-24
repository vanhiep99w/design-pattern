package com.designpatterns.showcase.strategy.context;

import com.designpatterns.showcase.strategy.discount.FixedDiscountStrategy;
import com.designpatterns.showcase.strategy.discount.PercentageDiscountStrategy;
import com.designpatterns.showcase.strategy.discount.SeasonalDiscountStrategy;
import com.designpatterns.showcase.strategy.dto.PricingRequest;
import com.designpatterns.showcase.strategy.dto.PricingResponse;
import com.designpatterns.showcase.strategy.shipping.ExpressShippingStrategy;
import com.designpatterns.showcase.strategy.shipping.InternationalShippingStrategy;
import com.designpatterns.showcase.strategy.shipping.StandardShippingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        DiscountContext discountContext = new DiscountContext(
                new PercentageDiscountStrategy(),
                new FixedDiscountStrategy(),
                new SeasonalDiscountStrategy()
        );

        ShippingContext shippingContext = new ShippingContext(
                new StandardShippingStrategy(),
                new ExpressShippingStrategy(),
                new InternationalShippingStrategy()
        );

        pricingService = new PricingService(discountContext, shippingContext);
    }

    @Test
    void shouldCalculateTotalPriceWithSpecificStrategies() {
        PricingRequest request = PricingRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("200.00"))
                .customerTier("GOLD")
                .orderDate(LocalDate.now())
                .discountStrategy("PERCENTAGE")
                .shippingStrategy("STANDARD")
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("2.0"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        PricingResponse response = pricingService.calculateTotalPrice(request);

        assertEquals(new BigDecimal("200.00"), response.getSubtotal());
        assertEquals(new BigDecimal("30.00"), response.getDiscountAmount());
        assertTrue(response.getShippingCost().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(response.getTotalAmount());
        assertNotNull(response.getDiscountDescription());
        assertNotNull(response.getShippingDescription());
        assertEquals(5, response.getEstimatedDeliveryDays());
    }

    @Test
    void shouldCalculateTotalPriceWithAutomaticSelection() {
        PricingRequest request = PricingRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("200.00"))
                .customerTier("GOLD")
                .orderDate(LocalDate.of(2024, 12, 25))
                .itemCount(5)
                .destinationCountry("CANADA")
                .packageWeight(new BigDecimal("3.0"))
                .isFragile(true)
                .requiresSignature(true)
                .build();

        PricingResponse response = pricingService.calculateTotalPrice(request);

        assertEquals(new BigDecimal("200.00"), response.getSubtotal());
        assertTrue(response.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getShippingCost().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(response.getTotalAmount());
    }

    @Test
    void shouldCalculateWithNoDiscount() {
        PricingRequest request = PricingRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("20.00"))
                .orderDate(LocalDate.now())
                .destinationCountry("USA")
                .packageWeight(new BigDecimal("1.0"))
                .isFragile(false)
                .requiresSignature(false)
                .build();

        PricingResponse response = pricingService.calculateTotalPrice(request);

        assertEquals(new BigDecimal("20.00"), response.getSubtotal());
        assertEquals(BigDecimal.ZERO, response.getDiscountAmount());
    }
}
