package com.designpatterns.showcase.strategy.context;

import com.designpatterns.showcase.strategy.discount.DiscountStrategy;
import com.designpatterns.showcase.strategy.dto.DiscountRequest;
import com.designpatterns.showcase.strategy.dto.DiscountResult;
import com.designpatterns.showcase.strategy.exception.UnsupportedStrategyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class DiscountContext {

    private final Map<String, DiscountStrategy> strategies;

    public DiscountContext(
            @Qualifier("percentageDiscount") DiscountStrategy percentageDiscount,
            @Qualifier("fixedDiscount") DiscountStrategy fixedDiscount,
            @Qualifier("seasonalDiscount") DiscountStrategy seasonalDiscount) {
        this.strategies = Map.of(
                "PERCENTAGE", percentageDiscount,
                "FIXED", fixedDiscount,
                "SEASONAL", seasonalDiscount
        );
    }

    public DiscountResult applyDiscount(String strategyType, DiscountRequest request) {
        log.info("Selecting discount strategy: {}", strategyType);

        DiscountStrategy strategy = strategies.get(strategyType.toUpperCase());
        if (strategy == null) {
            throw new UnsupportedStrategyException("Unknown discount strategy: " + strategyType);
        }

        return strategy.calculateDiscount(request);
    }

    public DiscountResult applyBestDiscount(DiscountRequest request) {
        log.info("Determining best discount for customer: {}", request.getCustomerId());

        DiscountResult bestDiscount = null;

        for (DiscountStrategy strategy : strategies.values()) {
            if (strategy.isApplicable(request)) {
                DiscountResult result = strategy.calculateDiscount(request);
                if (result.isApplied() && (bestDiscount == null
                        || result.getDiscountAmount().compareTo(bestDiscount.getDiscountAmount()) > 0)) {
                    bestDiscount = result;
                }
            }
        }

        if (bestDiscount == null) {
            log.info("No applicable discount found");
            return DiscountResult.builder()
                    .originalAmount(request.getOrderAmount())
                    .discountAmount(java.math.BigDecimal.ZERO)
                    .finalAmount(request.getOrderAmount())
                    .discountType("NONE")
                    .description("No discount applicable")
                    .applied(false)
                    .build();
        }

        log.info("Best discount selected: {} with amount: {}",
                bestDiscount.getDiscountType(),
                bestDiscount.getDiscountAmount());

        return bestDiscount;
    }
}
