package com.designpatterns.showcase.strategy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ShippingRequest {
    private String destinationCountry;
    private String destinationZipCode;
    private BigDecimal packageWeight;
    private BigDecimal packageVolume;
    private boolean isFragile;
    private boolean requiresSignature;
    private Integer estimatedDeliveryDays;
}
