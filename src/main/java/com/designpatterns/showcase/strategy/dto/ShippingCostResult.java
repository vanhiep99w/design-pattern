package com.designpatterns.showcase.strategy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ShippingCostResult {
    private BigDecimal baseCost;
    private BigDecimal additionalFees;
    private BigDecimal totalCost;
    private String shippingMethod;
    private Integer estimatedDeliveryDays;
    private String description;
}
