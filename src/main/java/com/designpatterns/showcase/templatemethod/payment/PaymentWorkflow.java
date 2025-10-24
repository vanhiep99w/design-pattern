package com.designpatterns.showcase.templatemethod.payment;

import com.designpatterns.showcase.templatemethod.dto.PaymentContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PaymentWorkflow {

    public final PaymentContext executePayment(PaymentContext context) {
        log.info("Starting payment workflow for transaction: {}", context.getTransactionId());
        
        validatePaymentDetails(context);
        
        if (!context.isValid()) {
            log.warn("Payment validation failed: {}", context.getErrorMessage());
            context.setSuccess(false);
            return context;
        }
        
        beforePaymentAuthorization(context);
        
        authorizePayment(context);
        
        if (!context.isAuthorized()) {
            log.warn("Payment authorization failed for transaction: {}", context.getTransactionId());
            context.setSuccess(false);
            return context;
        }
        
        capturePayment(context);
        
        if (!context.isCaptured()) {
            log.warn("Payment capture failed for transaction: {}", context.getTransactionId());
            handleCaptureFailure(context);
            context.setSuccess(false);
            return context;
        }
        
        recordTransaction(context);
        
        notifyCustomer(context);
        
        afterPaymentCompleted(context);
        
        context.setSuccess(true);
        log.info("Payment workflow completed successfully for transaction: {}", context.getTransactionId());
        
        return context;
    }

    protected abstract void validatePaymentDetails(PaymentContext context);

    protected abstract void authorizePayment(PaymentContext context);

    protected abstract void capturePayment(PaymentContext context);

    protected abstract void recordTransaction(PaymentContext context);

    protected abstract void notifyCustomer(PaymentContext context);

    protected void beforePaymentAuthorization(PaymentContext context) {
        log.debug("Default pre-authorization hook for transaction: {}", context.getTransactionId());
    }

    protected void handleCaptureFailure(PaymentContext context) {
        log.info("Default capture failure handling - reversing authorization if needed");
        context.addLog("Payment capture failed - authorization may need to be reversed");
    }

    protected void afterPaymentCompleted(PaymentContext context) {
        log.debug("Payment completion hook for transaction: {}", context.getTransactionId());
    }
}
