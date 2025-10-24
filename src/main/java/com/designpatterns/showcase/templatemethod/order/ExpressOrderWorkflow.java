package com.designpatterns.showcase.templatemethod.order;

import com.designpatterns.showcase.templatemethod.dto.OrderProcessingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class ExpressOrderWorkflow extends OrderWorkflow {

    private static final BigDecimal EXPRESS_FEE = BigDecimal.valueOf(25.00);

    @Override
    protected void validateOrder(OrderProcessingContext context) {
        log.info("Express validation for order: {}", context.getOrderId());
        context.addLog("Express validation started");
        
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
        context.addLog("Express validation passed - Priority handling enabled");
    }

    @Override
    protected void checkInventory(OrderProcessingContext context) {
        log.info("Priority inventory check for express order: {}", context.getOrderId());
        context.addLog("Express inventory check - checking express warehouse");
        
        context.setInventoryAvailable(true);
        context.addLog("Express inventory check passed - items available in express warehouse");
    }

    @Override
    protected void calculatePricing(OrderProcessingContext context) {
        log.info("Calculating express pricing for order: {}", context.getOrderId());
        context.addLog("Express pricing calculation started");
        
        BigDecimal baseAmount = context.getTotalAmount() != null 
            ? context.getTotalAmount() 
            : BigDecimal.valueOf(100.00);
        
        BigDecimal amountWithExpressFee = baseAmount.add(EXPRESS_FEE);
        BigDecimal tax = amountWithExpressFee.multiply(BigDecimal.valueOf(0.10));
        BigDecimal finalAmount = amountWithExpressFee.add(tax);
        
        context.setTotalAmount(baseAmount);
        context.setTax(tax);
        context.setFinalAmount(finalAmount);
        
        context.addLog("Express pricing calculated: Base=" + baseAmount + 
                      ", Express Fee=" + EXPRESS_FEE + ", Tax=" + tax + ", Final=" + finalAmount);
    }

    @Override
    protected void processPayment(OrderProcessingContext context) {
        log.info("Processing express payment for order: {}", context.getOrderId());
        context.addLog("Express payment processing - using priority payment gateway");
        
        String transactionId = "EXPRESS-TXN-" + UUID.randomUUID().toString();
        context.setPaymentTransactionId(transactionId);
        context.setPaymentSuccessful(true);
        
        context.addLog("Express payment processed: " + transactionId);
    }

    @Override
    protected void reserveInventory(OrderProcessingContext context) {
        log.info("Priority inventory reservation for express order: {}", context.getOrderId());
        context.addLog("Express inventory reserved with priority flag");
    }

    @Override
    protected void sendConfirmation(OrderProcessingContext context) {
        log.info("Sending express confirmation for order: {}", context.getOrderId());
        context.addLog("Express confirmation sent with SMS and email");
        
        String confirmationNumber = "EXP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        context.setConfirmationNumber(confirmationNumber);
    }

    @Override
    protected void afterOrderProcessed(OrderProcessingContext context) {
        log.info("Express order post-processing for order: {}", context.getOrderId());
        context.addLog("Express shipping label generated and warehouse notified");
    }
}
