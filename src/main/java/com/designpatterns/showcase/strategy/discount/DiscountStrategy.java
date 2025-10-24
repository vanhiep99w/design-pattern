package com.designpatterns.showcase.strategy.discount;

import com.designpatterns.showcase.strategy.dto.DiscountRequest;
import com.designpatterns.showcase.strategy.dto.DiscountResult;

public interface DiscountStrategy {
    DiscountResult calculateDiscount(DiscountRequest request);
    boolean isApplicable(DiscountRequest request);
    String getStrategyName();
}
