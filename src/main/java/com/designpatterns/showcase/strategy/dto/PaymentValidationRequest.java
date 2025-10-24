package com.designpatterns.showcase.strategy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class PaymentValidationRequest {
    private String customerId;
    private BigDecimal amount;
    private String paymentMethod;
    private String ipAddress;
    private String billingCountry;
    private Map<String, String> paymentDetails;
    private boolean isRecurring;
}
