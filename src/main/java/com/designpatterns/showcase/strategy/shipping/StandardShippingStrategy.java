package com.designpatterns.showcase.strategy.shipping;

import com.designpatterns.showcase.strategy.dto.ShippingRequest;
import com.designpatterns.showcase.strategy.dto.ShippingCostResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Slf4j
@Component("standardShipping")
public class StandardShippingStrategy implements ShippingCostStrategy {

    private static final BigDecimal BASE_COST = new BigDecimal("5.00");
    private static final BigDecimal COST_PER_KG = new BigDecimal("2.00");
    private static final BigDecimal FRAGILE_FEE = new BigDecimal("3.00");
    private static final BigDecimal SIGNATURE_FEE = new BigDecimal("2.00");
    private static final Integer ESTIMATED_DELIVERY_DAYS = 5;
    private static final Set<String> SUPPORTED_COUNTRIES = Set.of("USA", "US", "UNITED STATES");

    @Override
    public ShippingCostResult calculateShippingCost(ShippingRequest request) {
        log.info("Calculating standard shipping cost for destination: {}", request.getDestinationCountry());

        BigDecimal baseCost = BASE_COST;
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

        log.info("Standard shipping cost calculated: ${}", totalCost);

        return ShippingCostResult.builder()
                .baseCost(baseCost)
                .additionalFees(additionalFees)
                .totalCost(totalCost)
                .shippingMethod("STANDARD")
                .estimatedDeliveryDays(ESTIMATED_DELIVERY_DAYS)
                .description(String.format("Standard shipping (5-7 business days). %s",
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
        return "STANDARD";
    }
}
