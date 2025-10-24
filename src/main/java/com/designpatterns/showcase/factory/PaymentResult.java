package com.designpatterns.showcase.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String message;
    private BigDecimal processedAmount;
    private String paymentType;
    private LocalDateTime timestamp;
}
