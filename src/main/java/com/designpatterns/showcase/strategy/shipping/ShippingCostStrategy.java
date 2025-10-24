package com.designpatterns.showcase.strategy.shipping;

import com.designpatterns.showcase.strategy.dto.ShippingRequest;
import com.designpatterns.showcase.strategy.dto.ShippingCostResult;

public interface ShippingCostStrategy {
    ShippingCostResult calculateShippingCost(ShippingRequest request);
    boolean supportsDestination(String country);
    String getStrategyName();
}
