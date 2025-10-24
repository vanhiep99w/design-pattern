package com.designpatterns.showcase.templatemethod.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProcessingContext {
    private String orderId;
    private String customerId;
    private List<String> items;
    private BigDecimal totalAmount;
    private BigDecimal tax;
    private BigDecimal finalAmount;
    
    @Builder.Default
    private boolean valid = true;
    private String validationMessage;
    
    @Builder.Default
    private boolean inventoryAvailable = true;
    
    @Builder.Default
    private boolean paymentSuccessful = false;
    private String paymentTransactionId;
    
    @Builder.Default
    private boolean processed = false;
    
    private String confirmationNumber;
    
    @Builder.Default
    private List<String> processingLog = new ArrayList<>();
    
    public void addLog(String message) {
        processingLog.add(message);
    }
}
