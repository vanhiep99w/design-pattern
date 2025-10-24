package com.designpatterns.showcase.templatemethod.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentContext {
    private String transactionId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    
    @Builder.Default
    private boolean valid = true;
    private String errorMessage;
    
    @Builder.Default
    private boolean authorized = false;
    private String authorizationCode;
    
    @Builder.Default
    private boolean captured = false;
    private LocalDateTime capturedAt;
    
    @Builder.Default
    private boolean success = false;
    
    private String receiptNumber;
    
    @Builder.Default
    private List<String> processingLog = new ArrayList<>();
    
    public void addLog(String message) {
        processingLog.add(message);
    }
}
