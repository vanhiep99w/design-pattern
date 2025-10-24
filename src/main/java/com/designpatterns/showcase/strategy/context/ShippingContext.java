package com.designpatterns.showcase.strategy.context;

import com.designpatterns.showcase.strategy.dto.ShippingRequest;
import com.designpatterns.showcase.strategy.dto.ShippingCostResult;
import com.designpatterns.showcase.strategy.exception.UnsupportedStrategyException;
import com.designpatterns.showcase.strategy.shipping.ShippingCostStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ShippingContext {

    private final Map<String, ShippingCostStrategy> strategies;

    public ShippingContext(
            @Qualifier("standardShipping") ShippingCostStrategy standardShipping,
            @Qualifier("expressShipping") ShippingCostStrategy expressShipping,
            @Qualifier("internationalShipping") ShippingCostStrategy internationalShipping) {
        this.strategies = Map.of(
                "STANDARD", standardShipping,
                "EXPRESS", expressShipping,
                "INTERNATIONAL", internationalShipping
        );
    }

    public ShippingCostResult calculateShippingCost(String strategyType, ShippingRequest request) {
        log.info("Selecting shipping strategy: {}", strategyType);

        ShippingCostStrategy strategy = strategies.get(strategyType.toUpperCase());
        if (strategy == null) {
            throw new UnsupportedStrategyException("Unknown shipping strategy: " + strategyType);
        }

        if (!strategy.supportsDestination(request.getDestinationCountry())) {
            throw new UnsupportedStrategyException(
                    String.format("Strategy %s does not support destination: %s",
                            strategyType, request.getDestinationCountry()));
        }

        return strategy.calculateShippingCost(request);
    }

    public ShippingCostResult calculateShippingCostAuto(ShippingRequest request) {
        log.info("Auto-selecting shipping strategy for destination: {}", request.getDestinationCountry());

        for (ShippingCostStrategy strategy : strategies.values()) {
            if (strategy.supportsDestination(request.getDestinationCountry())) {
                log.info("Selected strategy: {}", strategy.getStrategyName());
                return strategy.calculateShippingCost(request);
            }
        }

        throw new UnsupportedStrategyException(
                "No shipping strategy available for destination: " + request.getDestinationCountry());
    }
}
