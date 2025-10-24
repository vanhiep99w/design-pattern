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
public class OrderProcessingRequest {
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String paymentType;
    private Map<String, String> paymentDetails;
    private String notificationType;
    private String notificationRecipient;
    private Map<String, String> notificationMetadata;
}
