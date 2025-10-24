package com.designpatterns.showcase.templatemethod.payment;

import com.designpatterns.showcase.templatemethod.dto.PaymentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class CreditCardPaymentWorkflow extends PaymentWorkflow {

    @Override
    protected void validatePaymentDetails(PaymentContext context) {
        log.info("Validating credit card payment details for transaction: {}", context.getTransactionId());
        context.addLog("Credit card validation started");
        
        if (context.getAmount() == null || context.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            context.setValid(false);
            context.setErrorMessage("Invalid payment amount");
            return;
        }
        
        if (context.getCustomerId() == null || context.getCustomerId().isEmpty()) {
            context.setValid(false);
            context.setErrorMessage("Customer ID is required");
            return;
        }
        
        context.setValid(true);
        context.addLog("Credit card validation passed");
    }

    @Override
    protected void beforePaymentAuthorization(PaymentContext context) {
        log.info("Pre-authorization fraud check for credit card transaction: {}", context.getTransactionId());
        context.addLog("Credit card fraud check completed");
    }

    @Override
    protected void authorizePayment(PaymentContext context) {
        log.info("Authorizing credit card payment for transaction: {}", context.getTransactionId());
        context.addLog("Contacting credit card processor for authorization");
        
        String authCode = "AUTH-CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        context.setAuthorizationCode(authCode);
        context.setAuthorized(true);
        
        context.addLog("Credit card authorized with code: " + authCode);
    }

    @Override
    protected void capturePayment(PaymentContext context) {
        log.info("Capturing credit card payment for transaction: {}", context.getTransactionId());
        context.addLog("Capturing funds from credit card");
        
        context.setCaptured(true);
        context.setCapturedAt(LocalDateTime.now());
        
        context.addLog("Credit card payment captured successfully");
    }

    @Override
    protected void recordTransaction(PaymentContext context) {
        log.info("Recording credit card transaction: {}", context.getTransactionId());
        context.addLog("Transaction recorded in payment ledger");
        
        String receiptNumber = "RECEIPT-CC-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        context.setReceiptNumber(receiptNumber);
    }

    @Override
    protected void notifyCustomer(PaymentContext context) {
        log.info("Notifying customer of credit card payment: {}", context.getTransactionId());
        context.addLog("Email receipt sent to customer");
    }

    @Override
    protected void afterPaymentCompleted(PaymentContext context) {
        log.info("Credit card payment post-processing for transaction: {}", context.getTransactionId());
        context.addLog("Credit card rewards points calculated and added");
    }
}
