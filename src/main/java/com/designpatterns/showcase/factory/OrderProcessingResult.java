package com.designpatterns.showcase.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProcessingResult {
    private boolean success;
    private String message;
    private PaymentResult paymentResult;
    private NotificationResult notificationResult;
}
