package com.designpatterns.showcase.strategy.shipping;

import com.designpatterns.showcase.strategy.dto.ShippingRequest;
import com.designpatterns.showcase.strategy.dto.ShippingCostResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component("internationalShipping")
public class InternationalShippingStrategy implements ShippingCostStrategy {

    private static final BigDecimal BASE_COST = new BigDecimal("25.00");
    private static final BigDecimal COST_PER_KG = new BigDecimal("8.00");
    private static final BigDecimal CUSTOMS_FEE = new BigDecimal("15.00");
    private static final BigDecimal FRAGILE_FEE = new BigDecimal("10.00");
    private static final BigDecimal SIGNATURE_FEE = new BigDecimal("5.00");
    private static final Integer DEFAULT_DELIVERY_DAYS = 10;
    
    private static final Set<String> DOMESTIC_COUNTRIES = Set.of("USA", "US", "UNITED STATES");
    private static final Map<String, Integer> DELIVERY_TIMES = Map.of(
        "CANADA", 7,
        "MEXICO", 8,
        "UK", 10,
        "UNITED KINGDOM", 10,
        "GERMANY", 12,
        "FRANCE", 12,
        "JAPAN", 14,
        "AUSTRALIA", 15,
        "CHINA", 14
    );

    @Override
    public ShippingCostResult calculateShippingCost(ShippingRequest request) {
        log.info("Calculating international shipping cost for destination: {}", request.getDestinationCountry());

        BigDecimal baseCost = BASE_COST;
        BigDecimal weightCost = request.getPackageWeight()
                .multiply(COST_PER_KG)
                .setScale(2, RoundingMode.HALF_UP);

        baseCost = baseCost.add(weightCost).add(CUSTOMS_FEE);

        BigDecimal additionalFees = BigDecimal.ZERO;
        StringBuilder feeDescription = new StringBuilder("Customs processing: $" + CUSTOMS_FEE + "; ");

        if (request.isFragile()) {
            additionalFees = additionalFees.add(FRAGILE_FEE);
            feeDescription.append("International fragile handling: $").append(FRAGILE_FEE).append("; ");
        }

        if (request.isRequiresSignature()) {
            additionalFees = additionalFees.add(SIGNATURE_FEE);
            feeDescription.append("Signature required: $").append(SIGNATURE_FEE).append("; ");
        }

        BigDecimal totalCost = baseCost.add(additionalFees).setScale(2, RoundingMode.HALF_UP);
        Integer deliveryDays = getEstimatedDeliveryDays(request.getDestinationCountry());

        log.info("International shipping cost calculated: ${} to {}", totalCost, request.getDestinationCountry());

        return ShippingCostResult.builder()
                .baseCost(baseCost)
                .additionalFees(additionalFees)
                .totalCost(totalCost)
                .shippingMethod("INTERNATIONAL")
                .estimatedDeliveryDays(deliveryDays)
                .description(String.format("International shipping to %s (%d-14 business days). %s",
                        request.getDestinationCountry(),
                        deliveryDays,
                        feeDescription))
                .build();
    }

    @Override
    public boolean supportsDestination(String country) {
        if (country == null) {
            return false;
        }
        return !DOMESTIC_COUNTRIES.contains(country.toUpperCase());
    }

    @Override
    public String getStrategyName() {
        return "INTERNATIONAL";
    }

    private Integer getEstimatedDeliveryDays(String country) {
        if (country == null) {
            return DEFAULT_DELIVERY_DAYS;
        }
        return DELIVERY_TIMES.getOrDefault(country.toUpperCase(), DEFAULT_DELIVERY_DAYS);
    }
}
