package com.designpatterns.showcase.templatemethod.payment;

import com.designpatterns.showcase.templatemethod.dto.PaymentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class CryptoPaymentWorkflow extends PaymentWorkflow {

    @Override
    protected void validatePaymentDetails(PaymentContext context) {
        log.info("Validating crypto payment details for transaction: {}", context.getTransactionId());
        context.addLog("Crypto payment validation started");
        
        if (context.getAmount() == null || context.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            context.setValid(false);
            context.setErrorMessage("Invalid payment amount");
            return;
        }
        
        if (context.getCurrency() == null || !context.getCurrency().matches("BTC|ETH|USDT")) {
            context.setValid(false);
            context.setErrorMessage("Unsupported cryptocurrency");
            return;
        }
        
        context.setValid(true);
        context.addLog("Crypto payment validation passed for currency: " + context.getCurrency());
    }

    @Override
    protected void beforePaymentAuthorization(PaymentContext context) {
        log.info("Verifying blockchain network status for transaction: {}", context.getTransactionId());
        context.addLog("Blockchain network is operational - proceeding with transaction");
    }

    @Override
    protected void authorizePayment(PaymentContext context) {
        log.info("Authorizing crypto payment for transaction: {}", context.getTransactionId());
        context.addLog("Generating blockchain transaction for " + context.getCurrency());
        
        String authCode = "BLOCKCHAIN-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
        context.setAuthorizationCode(authCode);
        context.setAuthorized(true);
        
        context.addLog("Crypto transaction authorized with blockchain hash: " + authCode);
    }

    @Override
    protected void capturePayment(PaymentContext context) {
        log.info("Waiting for blockchain confirmations for transaction: {}", context.getTransactionId());
        context.addLog("Monitoring blockchain for transaction confirmations");
        
        context.setCaptured(true);
        context.setCapturedAt(LocalDateTime.now());
        
        context.addLog("Crypto payment confirmed on blockchain (6 confirmations received)");
    }

    @Override
    protected void recordTransaction(PaymentContext context) {
        log.info("Recording crypto transaction: {}", context.getTransactionId());
        context.addLog("Transaction recorded with blockchain reference");
        
        String receiptNumber = "RECEIPT-CRYPTO-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        context.setReceiptNumber(receiptNumber);
    }

    @Override
    protected void notifyCustomer(PaymentContext context) {
        log.info("Notifying customer of crypto payment: {}", context.getTransactionId());
        context.addLog("Blockchain explorer link sent to customer");
    }

    @Override
    protected void handleCaptureFailure(PaymentContext context) {
        log.warn("Crypto payment capture failed - blockchain confirmation timeout");
        context.addLog("Blockchain confirmation timeout - transaction may still be pending");
        context.addLog("Customer notified to check blockchain explorer");
    }

    @Override
    protected void afterPaymentCompleted(PaymentContext context) {
        log.info("Crypto payment post-processing for transaction: {}", context.getTransactionId());
        context.addLog("Crypto wallet balance updated and tax reporting data generated");
    }
}
