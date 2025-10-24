package com.designpatterns.showcase.strategy.discount;

import com.designpatterns.showcase.strategy.dto.DiscountRequest;
import com.designpatterns.showcase.strategy.dto.DiscountResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component("fixedDiscount")
public class FixedDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal MIN_ORDER_AMOUNT = new BigDecimal("50.00");
    private static final BigDecimal FIXED_DISCOUNT_AMOUNT = new BigDecimal("10.00");
    private static final Integer MIN_ITEM_COUNT = 3;
    private static final BigDecimal BULK_ORDER_THRESHOLD = new BigDecimal("200.00");
    private static final BigDecimal BULK_DISCOUNT_AMOUNT = new BigDecimal("25.00");

    @Override
    public DiscountResult calculateDiscount(DiscountRequest request) {
        log.info("Calculating fixed discount for customer: {}", request.getCustomerId());

        if (!isApplicable(request)) {
            return buildNoDiscountResult(request);
        }

        BigDecimal discountAmount = calculateFixedAmount(request);
        BigDecimal finalAmount = request.getOrderAmount()
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        log.info("Applied fixed discount: ${}", discountAmount);

        return DiscountResult.builder()
                .originalAmount(request.getOrderAmount())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .discountType("FIXED")
                .description(getDescription(request, discountAmount))
                .applied(true)
                .build();
    }

    @Override
    public boolean isApplicable(DiscountRequest request) {
        if (request.getOrderAmount() == null) {
            return false;
        }

        if (request.getOrderAmount().compareTo(BULK_ORDER_THRESHOLD) >= 0) {
            return true;
        }

        if (request.getOrderAmount().compareTo(MIN_ORDER_AMOUNT) >= 0
                && request.getItemCount() != null
                && request.getItemCount() >= MIN_ITEM_COUNT) {
            return true;
        }

        return false;
    }

    @Override
    public String getStrategyName() {
        return "FIXED";
    }

    private BigDecimal calculateFixedAmount(DiscountRequest request) {
        if (request.getOrderAmount().compareTo(BULK_ORDER_THRESHOLD) >= 0) {
            return BULK_DISCOUNT_AMOUNT;
        }
        return FIXED_DISCOUNT_AMOUNT;
    }

    private String getDescription(DiscountRequest request, BigDecimal discountAmount) {
        if (request.getOrderAmount().compareTo(BULK_ORDER_THRESHOLD) >= 0) {
            return String.format("Bulk order discount: $%.2f off", discountAmount.doubleValue());
        }
        return String.format("Multi-item discount: $%.2f off", discountAmount.doubleValue());
    }

    private DiscountResult buildNoDiscountResult(DiscountRequest request) {
        return DiscountResult.builder()
                .originalAmount(request.getOrderAmount())
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(request.getOrderAmount())
                .discountType("FIXED")
                .description("No fixed discount applicable")
                .applied(false)
                .build();
    }
}
