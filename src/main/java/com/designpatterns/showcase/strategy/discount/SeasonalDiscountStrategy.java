package com.designpatterns.showcase.strategy.discount;

import com.designpatterns.showcase.strategy.dto.DiscountRequest;
import com.designpatterns.showcase.strategy.dto.DiscountResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;

@Slf4j
@Component("seasonalDiscount")
public class SeasonalDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal HOLIDAY_DISCOUNT_RATE = new BigDecimal("0.25");
    private static final BigDecimal SUMMER_DISCOUNT_RATE = new BigDecimal("0.15");
    private static final BigDecimal SPRING_DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal MIN_ORDER_AMOUNT = new BigDecimal("75.00");

    @Override
    public DiscountResult calculateDiscount(DiscountRequest request) {
        log.info("Calculating seasonal discount for order date: {}", request.getOrderDate());

        if (!isApplicable(request)) {
            return buildNoDiscountResult(request);
        }

        SeasonalPeriod period = determineSeasonalPeriod(request.getOrderDate());
        BigDecimal discountRate = getDiscountRateForPeriod(period);

        BigDecimal discountAmount = request.getOrderAmount()
                .multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalAmount = request.getOrderAmount()
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Applied {} seasonal discount: {}%", period, discountRate.multiply(new BigDecimal("100")));

        return DiscountResult.builder()
                .originalAmount(request.getOrderAmount())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .discountType("SEASONAL")
                .description(String.format("%s sale: %.0f%% off",
                        period.getName(),
                        discountRate.multiply(new BigDecimal("100")).doubleValue()))
                .applied(true)
                .build();
    }

    @Override
    public boolean isApplicable(DiscountRequest request) {
        if (request.getOrderAmount() == null || request.getOrderDate() == null) {
            return false;
        }

        if (request.getOrderAmount().compareTo(MIN_ORDER_AMOUNT) < 0) {
            return false;
        }

        SeasonalPeriod period = determineSeasonalPeriod(request.getOrderDate());
        return period != SeasonalPeriod.REGULAR;
    }

    @Override
    public String getStrategyName() {
        return "SEASONAL";
    }

    private SeasonalPeriod determineSeasonalPeriod(LocalDate date) {
        Month month = date.getMonth();
        int day = date.getDayOfMonth();

        if ((month == Month.NOVEMBER && day >= 20) || (month == Month.DECEMBER && day <= 31)) {
            return SeasonalPeriod.HOLIDAY;
        }

        if (month == Month.JUNE || month == Month.JULY || month == Month.AUGUST) {
            return SeasonalPeriod.SUMMER;
        }

        if (month == Month.MARCH || month == Month.APRIL || month == Month.MAY) {
            return SeasonalPeriod.SPRING;
        }

        return SeasonalPeriod.REGULAR;
    }

    private BigDecimal getDiscountRateForPeriod(SeasonalPeriod period) {
        return switch (period) {
            case HOLIDAY -> HOLIDAY_DISCOUNT_RATE;
            case SUMMER -> SUMMER_DISCOUNT_RATE;
            case SPRING -> SPRING_DISCOUNT_RATE;
            case REGULAR -> BigDecimal.ZERO;
        };
    }

    private DiscountResult buildNoDiscountResult(DiscountRequest request) {
        return DiscountResult.builder()
                .originalAmount(request.getOrderAmount())
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(request.getOrderAmount())
                .discountType("SEASONAL")
                .description("No seasonal discount available")
                .applied(false)
                .build();
    }

    private enum SeasonalPeriod {
        HOLIDAY("Holiday Season"),
        SUMMER("Summer Sale"),
        SPRING("Spring Sale"),
        REGULAR("Regular");

        private final String name;

        SeasonalPeriod(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
