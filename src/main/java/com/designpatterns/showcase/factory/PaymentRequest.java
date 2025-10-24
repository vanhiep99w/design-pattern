package com.designpatterns.showcase.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String paymentType;
    private BigDecimal amount;
    private String currency;
    private String customerId;
    private Map<String, String> paymentDetails;
}
