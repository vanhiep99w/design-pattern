package com.designpatterns.showcase.strategy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PricingRequest {
    private String customerId;
    private BigDecimal orderAmount;
    private String customerTier;
    private LocalDate orderDate;
    private String productCategory;
    private Integer itemCount;
    private boolean isFirstOrder;
    private String discountStrategy;
    private String shippingStrategy;
    private String destinationCountry;
    private String destinationZipCode;
    private BigDecimal packageWeight;
    private BigDecimal packageVolume;
    private boolean isFragile;
    private boolean requiresSignature;
}
