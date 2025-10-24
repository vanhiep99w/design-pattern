package com.designpatterns.showcase.strategy.discount;

import com.designpatterns.showcase.strategy.dto.DiscountRequest;
import com.designpatterns.showcase.strategy.dto.DiscountResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component("percentageDiscount")
public class PercentageDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal MIN_ORDER_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal BRONZE_DISCOUNT = new BigDecimal("0.05");
    private static final BigDecimal SILVER_DISCOUNT = new BigDecimal("0.10");
    private static final BigDecimal GOLD_DISCOUNT = new BigDecimal("0.15");
    private static final BigDecimal PLATINUM_DISCOUNT = new BigDecimal("0.20");

    @Override
    public DiscountResult calculateDiscount(DiscountRequest request) {
        log.info("Calculating percentage discount for customer: {}", request.getCustomerId());

        if (!isApplicable(request)) {
            return buildNoDiscountResult(request);
        }

        BigDecimal discountRate = getDiscountRateByTier(request.getCustomerTier());
        BigDecimal discountAmount = request.getOrderAmount()
                .multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalAmount = request.getOrderAmount()
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Applied {}% discount for tier: {}, discount amount: {}",
                discountRate.multiply(new BigDecimal("100")),
                request.getCustomerTier(),
                discountAmount);

        return DiscountResult.builder()
                .originalAmount(request.getOrderAmount())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .discountType("PERCENTAGE")
                .description(String.format("%s tier: %.0f%% discount",
                        request.getCustomerTier(),
                        discountRate.multiply(new BigDecimal("100")).doubleValue()))
                .applied(true)
                .build();
    }

    @Override
    public boolean isApplicable(DiscountRequest request) {
        if (request.getOrderAmount() == null || request.getCustomerTier() == null) {
            return false;
        }
        return request.getOrderAmount().compareTo(MIN_ORDER_AMOUNT) >= 0;
    }

    @Override
    public String getStrategyName() {
        return "PERCENTAGE";
    }

    private BigDecimal getDiscountRateByTier(String tier) {
        if (tier == null) {
            return BigDecimal.ZERO;
        }

        return switch (tier.toUpperCase()) {
            case "PLATINUM" -> PLATINUM_DISCOUNT;
            case "GOLD" -> GOLD_DISCOUNT;
            case "SILVER" -> SILVER_DISCOUNT;
            case "BRONZE" -> BRONZE_DISCOUNT;
            default -> BigDecimal.ZERO;
        };
    }

    private DiscountResult buildNoDiscountResult(DiscountRequest request) {
        return DiscountResult.builder()
                .originalAmount(request.getOrderAmount())
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(request.getOrderAmount())
                .discountType("PERCENTAGE")
                .description("No percentage discount applicable")
                .applied(false)
                .build();
    }
}
