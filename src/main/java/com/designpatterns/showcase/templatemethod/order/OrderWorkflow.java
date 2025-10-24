package com.designpatterns.showcase.templatemethod.order;

import com.designpatterns.showcase.templatemethod.dto.OrderProcessingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class OrderWorkflow {

    public final OrderProcessingContext processOrder(OrderProcessingContext context) {
        log.info("Starting order processing workflow for order: {}", context.getOrderId());
        
        validateOrder(context);
        
        if (!context.isValid()) {
            log.warn("Order validation failed: {}", context.getValidationMessage());
            return context;
        }
        
        checkInventory(context);
        
        if (!context.isInventoryAvailable()) {
            log.warn("Inventory check failed for order: {}", context.getOrderId());
            context.setProcessed(false);
            return context;
        }
        
        calculatePricing(context);
        
        processPayment(context);
        
        if (!context.isPaymentSuccessful()) {
            log.warn("Payment processing failed for order: {}", context.getOrderId());
            handlePaymentFailure(context);
            context.setProcessed(false);
            return context;
        }
        
        reserveInventory(context);
        
        sendConfirmation(context);
        
        afterOrderProcessed(context);
        
        context.setProcessed(true);
        log.info("Order processing completed successfully for order: {}", context.getOrderId());
        
        return context;
    }

    protected abstract void validateOrder(OrderProcessingContext context);

    protected abstract void checkInventory(OrderProcessingContext context);

    protected abstract void calculatePricing(OrderProcessingContext context);

    protected abstract void processPayment(OrderProcessingContext context);

    protected abstract void reserveInventory(OrderProcessingContext context);

    protected abstract void sendConfirmation(OrderProcessingContext context);

    protected void handlePaymentFailure(OrderProcessingContext context) {
        log.info("Default payment failure handling for order: {}", context.getOrderId());
    }

    protected void afterOrderProcessed(OrderProcessingContext context) {
        log.debug("Order processing complete hook for order: {}", context.getOrderId());
    }
}
