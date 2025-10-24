package com.designpatterns.showcase.strategy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PricingResponse {
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String discountDescription;
    private String shippingDescription;
    private Integer estimatedDeliveryDays;
}
