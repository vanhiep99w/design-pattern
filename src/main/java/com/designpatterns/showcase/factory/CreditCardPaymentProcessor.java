package com.designpatterns.showcase.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component("creditCardProcessor")
public class CreditCardPaymentProcessor implements PaymentProcessor {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/([0-9]{2})$");
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("50000.00");

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing credit card payment for customer: {}", request.getCustomerId());

        if (!validatePaymentDetails(request)) {
            return PaymentResult.builder()
                    .success(false)
                    .message("Invalid credit card details")
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

        String transactionId = "CC-" + UUID.randomUUID().toString();
        log.info("Credit card payment processed successfully. Transaction ID: {}", transactionId);

        return PaymentResult.builder()
                .success(true)
                .transactionId(transactionId)
                .message("Payment processed successfully via Credit Card")
                .processedAmount(request.getAmount())
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

        String cardNumber = details.get("cardNumber");
        String cvv = details.get("cvv");
        String expiryDate = details.get("expiryDate");
        String cardHolderName = details.get("cardHolderName");

        if (cardNumber == null || !CARD_NUMBER_PATTERN.matcher(cardNumber).matches()) {
            log.warn("Invalid card number format");
            return false;
        }

        if (!isValidLuhn(cardNumber)) {
            log.warn("Card number failed Luhn check");
            return false;
        }

        if (cvv == null || !CVV_PATTERN.matcher(cvv).matches()) {
            log.warn("Invalid CVV format");
            return false;
        }

        if (expiryDate == null || !EXPIRY_PATTERN.matcher(expiryDate).matches()) {
            log.warn("Invalid expiry date format");
            return false;
        }

        if (!isCardNotExpired(expiryDate)) {
            log.warn("Card is expired");
            return false;
        }

        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            log.warn("Card holder name is missing");
            return false;
        }

        return true;
    }

    @Override
    public String getPaymentType() {
        return "CREDIT_CARD";
    }

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private boolean isCardNotExpired(String expiryDate) {
        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]);
        
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        
        return year > currentYear || (year == currentYear && month >= currentMonth);
    }
}
