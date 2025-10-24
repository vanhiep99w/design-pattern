package com.designpatterns.showcase.strategy.shipping;

import com.designpatterns.showcase.strategy.dto.ShippingRequest;
import com.designpatterns.showcase.strategy.dto.ShippingCostResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Slf4j
@Component("expressShipping")
public class ExpressShippingStrategy implements ShippingCostStrategy {

    private static final BigDecimal BASE_COST = new BigDecimal("15.00");
    private static final BigDecimal COST_PER_KG = new BigDecimal("5.00");
    private static final BigDecimal FRAGILE_FEE = new BigDecimal("5.00");
    private static final BigDecimal SIGNATURE_FEE = new BigDecimal("3.00");
    private static final BigDecimal PRIORITY_HANDLING_FEE = new BigDecimal("10.00");
    private static final Integer ESTIMATED_DELIVERY_DAYS = 2;
    private static final Set<String> SUPPORTED_COUNTRIES = Set.of("USA", "US", "UNITED STATES");

    @Override
    public ShippingCostResult calculateShippingCost(ShippingRequest request) {
        log.info("Calculating express shipping cost for destination: {}", request.getDestinationCountry());

        BigDecimal baseCost = BASE_COST.add(PRIORITY_HANDLING_FEE);
        BigDecimal weightCost = request.getPackageWeight()
                .multiply(COST_PER_KG)
                .setScale(2, RoundingMode.HALF_UP);

        baseCost = baseCost.add(weightCost);

        BigDecimal additionalFees = BigDecimal.ZERO;
        StringBuilder feeDescription = new StringBuilder();

        if (request.isFragile()) {
            additionalFees = additionalFees.add(FRAGILE_FEE);
            feeDescription.append("Fragile handling: $").append(FRAGILE_FEE).append("; ");
        }

        if (request.isRequiresSignature()) {
            additionalFees = additionalFees.add(SIGNATURE_FEE);
            feeDescription.append("Signature required: $").append(SIGNATURE_FEE).append("; ");
        }

        BigDecimal totalCost = baseCost.add(additionalFees).setScale(2, RoundingMode.HALF_UP);

        log.info("Express shipping cost calculated: ${}", totalCost);

        return ShippingCostResult.builder()
                .baseCost(baseCost)
                .additionalFees(additionalFees)
                .totalCost(totalCost)
                .shippingMethod("EXPRESS")
                .estimatedDeliveryDays(ESTIMATED_DELIVERY_DAYS)
                .description(String.format("Express shipping (1-2 business days). Priority handling included. %s",
                        feeDescription.length() > 0 ? "Additional fees: " + feeDescription : "No additional fees."))
                .build();
    }

    @Override
    public boolean supportsDestination(String country) {
        if (country == null) {
            return false;
        }
        return SUPPORTED_COUNTRIES.contains(country.toUpperCase());
    }

    @Override
    public String getStrategyName() {
        return "EXPRESS";
    }
}
