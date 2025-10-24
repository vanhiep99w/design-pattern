package com.designpatterns.showcase.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component("payPalProcessor")
public class PayPalPaymentProcessor implements PaymentProcessor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final BigDecimal PAYPAL_FEE_PERCENTAGE = new BigDecimal("0.029");
    private static final BigDecimal PAYPAL_FIXED_FEE = new BigDecimal("0.30");
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("100000.00");

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing PayPal payment for customer: {}", request.getCustomerId());

        if (!validatePaymentDetails(request)) {
            return PaymentResult.builder()
                    .success(false)
                    .message("Invalid PayPal account details")
                    .paymentType(getPaymentType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getAmount().compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            return PaymentResult.builder()
                    .success(false)
                    .message("Transaction amount is below minimum of " + MIN_TRANSACTION_AMOUNT)
                    .paymentType(getPaymentType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getAmount().compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            return PaymentResult.builder()
                    .success(false)
                    .message("Transaction amount exceeds limit of " + MAX_TRANSACTION_AMOUNT)
                    .paymentType(getPaymentType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        BigDecimal fee = request.getAmount().multiply(PAYPAL_FEE_PERCENTAGE).add(PAYPAL_FIXED_FEE);
        BigDecimal netAmount = request.getAmount().subtract(fee);

        String transactionId = "PP-" + UUID.randomUUID().toString();
        log.info("PayPal payment processed successfully. Transaction ID: {}, Fee: {}", transactionId, fee);

        return PaymentResult.builder()
                .success(true)
                .transactionId(transactionId)
                .message("Payment processed successfully via PayPal. Fee: $" + fee)
                .processedAmount(netAmount)
                .paymentType(getPaymentType())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean validatePaymentDetails(PaymentRequest request) {
        Map<String, String> details = request.getPaymentDetails();
        
        if (details == null) {
            log.warn("Payment details are null");
            return false;
        }

        String email = details.get("email");
        String accountId = details.get("accountId");

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            log.warn("Invalid email format");
            return false;
        }

        if (accountId == null || accountId.trim().isEmpty()) {
            log.warn("PayPal account ID is missing");
            return false;
        }

        if (!accountId.matches("^[A-Z0-9]{13,17}$")) {
            log.warn("Invalid PayPal account ID format");
            return false;
        }

        return true;
    }

    @Override
    public String getPaymentType() {
        return "PAYPAL";
    }
}
