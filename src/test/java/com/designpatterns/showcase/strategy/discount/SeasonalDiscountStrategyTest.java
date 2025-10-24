package com.designpatterns.showcase.strategy.discount;

import com.designpatterns.showcase.strategy.dto.DiscountRequest;
import com.designpatterns.showcase.strategy.dto.DiscountResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SeasonalDiscountStrategyTest {

    private SeasonalDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SeasonalDiscountStrategy();
    }

    @Test
    void shouldApplyHolidaySeasonDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST001")
                .orderAmount(new BigDecimal("200.00"))
                .orderDate(LocalDate.of(2024, 12, 25))
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("50.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("150.00"), result.getFinalAmount());
        assertEquals("SEASONAL", result.getDiscountType());
        assertTrue(result.getDescription().contains("Holiday"));
        assertTrue(result.getDescription().contains("25%"));
    }

    @Test
    void shouldApplySummerSaleDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST002")
                .orderAmount(new BigDecimal("300.00"))
                .orderDate(LocalDate.of(2024, 7, 15))
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("45.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("255.00"), result.getFinalAmount());
        assertTrue(result.getDescription().contains("Summer"));
        assertTrue(result.getDescription().contains("15%"));
    }

    @Test
    void shouldApplySpringSaleDiscount() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST003")
                .orderAmount(new BigDecimal("150.00"))
                .orderDate(LocalDate.of(2024, 4, 10))
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertTrue(result.isApplied());
        assertEquals(new BigDecimal("15.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("135.00"), result.getFinalAmount());
        assertTrue(result.getDescription().contains("Spring"));
        assertTrue(result.getDescription().contains("10%"));
    }

    @Test
    void shouldNotApplyDiscountInRegularSeason() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST004")
                .orderAmount(new BigDecimal("200.00"))
                .orderDate(LocalDate.of(2024, 2, 15))
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertFalse(result.isApplied());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals(new BigDecimal("200.00"), result.getFinalAmount());
    }

    @Test
    void shouldNotApplyDiscountWhenAmountBelowMinimum() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST005")
                .orderAmount(new BigDecimal("50.00"))
                .orderDate(LocalDate.of(2024, 12, 25))
                .build();

        DiscountResult result = strategy.calculateDiscount(request);

        assertFalse(result.isApplied());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
    }

    @Test
    void shouldNotBeApplicableWhenAmountIsNull() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST006")
                .orderDate(LocalDate.of(2024, 12, 25))
                .build();

        assertFalse(strategy.isApplicable(request));
    }

    @Test
    void shouldNotBeApplicableWhenDateIsNull() {
        DiscountRequest request = DiscountRequest.builder()
                .customerId("CUST007")
                .orderAmount(new BigDecimal("200.00"))
                .build();

        assertFalse(strategy.isApplicable(request));
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("SEASONAL", strategy.getStrategyName());
    }
}
