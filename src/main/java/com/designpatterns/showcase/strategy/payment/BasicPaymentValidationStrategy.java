package com.designpatterns.showcase.strategy.payment;

import com.designpatterns.showcase.strategy.dto.PaymentValidationRequest;
import com.designpatterns.showcase.strategy.dto.PaymentValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("basicPaymentValidation")
public class BasicPaymentValidationStrategy implements PaymentValidationStrategy {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    @Override
    public PaymentValidationResult validate(PaymentValidationRequest request) {
        log.info("Performing basic payment validation for customer: {}", request.getCustomerId());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (request.getAmount() == null) {
            errors.add("Payment amount is required");
        } else {
            if (request.getAmount().compareTo(MIN_AMOUNT) < 0) {
                errors.add("Payment amount must be at least $" + MIN_AMOUNT);
            }
            if (request.getAmount().compareTo(MAX_AMOUNT) > 0) {
                errors.add("Payment amount exceeds maximum limit of $" + MAX_AMOUNT);
            }
        }

        if (request.getCustomerId() == null || request.getCustomerId().trim().isEmpty()) {
            errors.add("Customer ID is required");
        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            errors.add("Payment method is required");
        }

        if (request.getPaymentDetails() == null || request.getPaymentDetails().isEmpty()) {
            errors.add("Payment details are required");
        }

        if (request.getBillingCountry() == null || request.getBillingCountry().trim().isEmpty()) {
            warnings.add("Billing country not provided");
        }

        boolean isValid = errors.isEmpty();
        log.info("Basic validation result: {}", isValid ? "PASSED" : "FAILED");

        return PaymentValidationResult.builder()
                .valid(isValid)
                .validationType("BASIC")
                .errors(errors)
                .warnings(warnings)
                .riskScore(0)
                .message(isValid ? "Basic validation passed" : "Basic validation failed")
                .build();
    }

    @Override
    public String getStrategyName() {
        return "BASIC";
    }
}
