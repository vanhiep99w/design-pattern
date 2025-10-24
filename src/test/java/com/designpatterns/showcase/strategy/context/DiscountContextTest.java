package com.designpatterns.showcase.strategy.context;

import com.designpatterns.showcase.strategy.discount.FixedDiscountStrategy;
import com.designpatterns.showcase.strategy.discount.PercentageDiscountStrategy;
import com.designpatterns.showcase.strategy.discount.SeasonalDiscountStrategy;
import com.designpatterns.showcase.strategy.dto.DiscountRequest;
import com.designpatterns.showcase.strategy.dto.DiscountResult;
import com.designpatterns.showcase.strategy.exception.UnsupportedStrategyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DiscountContextTest {

    private DiscountContext context;

    @BeforeEach
    void setUp() {
        context = new DiscountContext(
                new PercentageDiscountStrategy(),
                new FixedDiscountStrategy(),
                new SeasonalDiscountStrategy()
        );
    }

    @Test
    void shouldApplyPercentageDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("200.00"))
                .customerTier("GOLD")
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = context.applyDiscount("PERCENTAGE", request);

        assertTrue(result.isApplied());
        assertEquals("PERCENTAGE", result.getDiscountType());
        assertEquals(new BigDecimal("30.00"), result.getDiscountAmount());
    }

    @Test
    void shouldApplyFixedDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("80.00"))
                .itemCount(3)
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = context.applyDiscount("FIXED", request);

        assertTrue(result.isApplied());
        assertEquals("FIXED", result.getDiscountType());
    }

    @Test
    void shouldApplySeasonalDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("200.00"))
                .orderDate(LocalDate.of(2024, 12, 25))
                .build();

        DiscountResult result = context.applyDiscount("SEASONAL", request);

        assertTrue(result.isApplied());
        assertEquals("SEASONAL", result.getDiscountType());
    }

    @Test
    void shouldThrowExceptionForUnknownStrategy() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("100.00"))
                .build();

        assertThrows(UnsupportedStrategyException.class, () -> {
            context.applyDiscount("UNKNOWN", request);
        });
    }

    @Test
    void shouldFindBestDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("200.00"))
                .customerTier("GOLD")
                .orderDate(LocalDate.of(2024, 12, 25))
                .itemCount(5)
                .build();

        DiscountResult result = context.applyBestDiscount(request);

        assertTrue(result.isApplied());
        assertTrue(result.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldReturnNoDiscountWhenNoneApplicable() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("10.00"))
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = context.applyBestDiscount(request);

        assertFalse(result.isApplied());
        assertEquals("NONE", result.getDiscountType());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
    }
}
