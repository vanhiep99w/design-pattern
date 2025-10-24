package com.designpatterns.showcase.templatemethod.order;

import com.designpatterns.showcase.templatemethod.dto.OrderProcessingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class StandardOrderWorkflow extends OrderWorkflow {

    @Override
    protected void validateOrder(OrderProcessingContext context) {
        log.info("Standard validation for order: {}", context.getOrderId());
        context.addLog("Standard validation started");
        
        if (context.getCustomerId() == null || context.getCustomerId().isEmpty()) {
            context.setValid(false);
            context.setValidationMessage("Customer ID is required");
            return;
        }
        
        if (context.getItems() == null || context.getItems().isEmpty()) {
            context.setValid(false);
            context.setValidationMessage("Order must contain at least one item");
            return;
        }
        
        context.setValid(true);
        context.addLog("Standard validation passed");
    }

    @Override
    protected void checkInventory(OrderProcessingContext context) {
        log.info("Checking inventory for order: {}", context.getOrderId());
        context.addLog("Standard inventory check started");
        
        context.setInventoryAvailable(true);
        context.addLog("Standard inventory check passed");
    }

    @Override
    protected void calculatePricing(OrderProcessingContext context) {
        log.info("Calculating pricing for order: {}", context.getOrderId());
        context.addLog("Standard pricing calculation started");
        
        BigDecimal baseAmount = context.getTotalAmount() != null 
            ? context.getTotalAmount() 
            : BigDecimal.valueOf(100.00);
        
        BigDecimal tax = baseAmount.multiply(BigDecimal.valueOf(0.10));
        BigDecimal finalAmount = baseAmount.add(tax);
        
        context.setTotalAmount(baseAmount);
        context.setTax(tax);
        context.setFinalAmount(finalAmount);
        
        context.addLog("Standard pricing calculated: Base=" + baseAmount + ", Tax=" + tax + ", Final=" + finalAmount);
    }

    @Override
    protected void processPayment(OrderProcessingContext context) {
        log.info("Processing standard payment for order: {}", context.getOrderId());
        context.addLog("Standard payment processing started");
        
        String transactionId = "TXN-" + UUID.randomUUID().toString();
        context.setPaymentTransactionId(transactionId);
        context.setPaymentSuccessful(true);
        
        context.addLog("Standard payment processed: " + transactionId);
    }

    @Override
    protected void reserveInventory(OrderProcessingContext context) {
        log.info("Reserving inventory for order: {}", context.getOrderId());
        context.addLog("Standard inventory reservation completed");
    }

    @Override
    protected void sendConfirmation(OrderProcessingContext context) {
        log.info("Sending standard confirmation for order: {}", context.getOrderId());
        context.addLog("Standard confirmation sent");
        
        String confirmationNumber = "CONF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        context.setConfirmationNumber(confirmationNumber);
    }
}
