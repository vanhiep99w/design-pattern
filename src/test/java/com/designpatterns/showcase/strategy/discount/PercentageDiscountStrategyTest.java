package com.designpatterns.showcase.strategy.discount;

import com.designpatterns.showcase.strategy.dto.DiscountRequest;
import com.designpatterns.showcase.strategy.dto.DiscountResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PercentageDiscountStrategyTest {

    private PercentageDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PercentageDiscountStrategy();
    }

    @Test
    void shouldApplyPlatinumTierDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("500.00"))
                .customerTier("PLATINUM")
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("100.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("400.00"), result.getFinalAmount());
        assertEquals("PERCENTAGE", result.getDiscountType());
        assertTrue(result.getDescription().contains("20%"));
    }

    @Test
    void shouldApplyGoldTierDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST002")
                .orderAmount(new BigDecimal("200.00"))
                .customerTier("GOLD")
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("30.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("170.00"), result.getFinalAmount());
        assertTrue(result.getDescription().contains("15%"));
    }

    @Test
    void shouldApplySilverTierDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST003")
                .orderAmount(new BigDecimal("150.00"))
                .customerTier("SILVER")
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("15.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("135.00"), result.getFinalAmount());
        assertTrue(result.getDescription().contains("10%"));
    }

    @Test
    void shouldApplyBronzeTierDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST004")
                .orderAmount(new BigDecimal("100.00"))
                .customerTier("BRONZE")
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("5.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("95.00"), result.getFinalAmount());
        assertTrue(result.getDescription().contains("5%"));
    }

    @Test
    void shouldNotApplyDiscountWhenAmountBelowMinimum() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST005")
                .orderAmount(new BigDecimal("50.00"))
                .customerTier("GOLD")
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertFalse(result.isApplied());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals(new BigDecimal("50.00"), result.getFinalAmount());
    }

    @Test
    void shouldNotApplyDiscountForUnknownTier() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST006")
                .orderAmount(new BigDecimal("200.00"))
                .customerTier("UNKNOWN")
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertFalse(result.isApplied());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
    }

    @Test
    void shouldNotBeApplicableWhenAmountIsNull() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST007")
                .customerTier("GOLD")
                .build();

        assertFalse(strategy.isApplicable(request));
    }

    @Test
    void shouldNotBeApplicableWhenTierIsNull() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST008")
                .orderAmount(new BigDecimal("200.00"))
                .build();

        assertFalse(strategy.isApplicable(request));
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("PERCENTAGE", strategy.getStrategyName());
    }
}
