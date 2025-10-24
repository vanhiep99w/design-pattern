package com.designpatterns.showcase.strategy.discount;

import com.designpatterns.showcase.strategy.dto.DiscountRequest;
import com.designpatterns.showcase.strategy.dto.DiscountResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FixedDiscountStrategyTest {

    private FixedDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FixedDiscountStrategy();
    }

    @Test
    void shouldApplyBulkOrderDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("250.00"))
                .itemCount(5)
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("25.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("225.00"), result.getFinalAmount());
        assertEquals("FIXED", result.getDiscountType());
        assertTrue(result.getDescription().contains("Bulk order"));
    }

    @Test
    void shouldApplyMultiItemDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST002")
                .orderAmount(new BigDecimal("80.00"))
                .itemCount(3)
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("10.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("70.00"), result.getFinalAmount());
        assertTrue(result.getDescription().contains("Multi-item"));
    }

    @Test
    void shouldNotApplyDiscountWhenAmountTooLow() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST003")
                .orderAmount(new BigDecimal("40.00"))
                .itemCount(3)
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertFalse(result.isApplied());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals(new BigDecimal("40.00"), result.getFinalAmount());
    }

    @Test
    void shouldNotApplyDiscountWhenItemCountTooLow() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST004")
                .orderAmount(new BigDecimal("80.00"))
                .itemCount(2)
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertFalse(result.isApplied());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
    }

    @Test
    void shouldApplyBulkDiscountEvenWithLowItemCount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST005")
                .orderAmount(new BigDecimal("300.00"))
                .itemCount(1)
                .orderDate(LocalDate.now())
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("25.00"), result.getDiscountAmount());
    }

    @Test
    void shouldNotBeApplicableWhenAmountIsNull() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST006")
                .itemCount(5)
                .build();

        assertFalse(strategy.isApplicable(request));
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("FIXED", strategy.getStrategyName());
    }
}
