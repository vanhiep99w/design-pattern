package com.designpatterns.showcase.strategy.payment;

import com.designpatterns.showcase.strategy.dto.PaymentValidationRequest;
import com.designpatterns.showcase.strategy.dto.PaymentValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component("fraudDetectionValidation")
public class FraudDetectionValidationStrategy implements PaymentValidationStrategy {

    private static final BigDecimal HIGH_RISK_AMOUNT = new BigDecimal("1000.00");
    private static final BigDecimal VERY_HIGH_RISK_AMOUNT = new BigDecimal("5000.00");
    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("XYZ", "ABC");
    private static final Set<String> SUSPICIOUS_IP_PREFIXES = Set.of("192.168", "10.0");

    @Override
    public PaymentValidationResult validate(PaymentValidationRequest request) {
        log.info("Performing fraud detection validation for customer: {}", request.getCustomerId());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int riskScore = 0;

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(VERY_HIGH_RISK_AMOUNT) > 0) {
                riskScore += 50;
                warnings.add("Very high transaction amount detected");
            } else if (request.getAmount().compareTo(HIGH_RISK_AMOUNT) > 0) {
                riskScore += 25;
                warnings.add("High transaction amount detected");
            }
        }

        if (request.getBillingCountry() != null
                && HIGH_RISK_COUNTRIES.contains(request.getBillingCountry().toUpperCase())) {
            riskScore += 30;
            warnings.add("Transaction from high-risk country");
        }

        if (request.getIpAddress() != null) {
            for (String prefix : SUSPICIOUS_IP_PREFIXES) {
                if (request.getIpAddress().startsWith(prefix)) {
                    riskScore += 20;
                    warnings.add("Suspicious IP address detected");
                    break;
                }
            }
        } else {
            riskScore += 10;
            warnings.add("IP address not provided");
        }

        if (request.getPaymentDetails() != null) {
            String cardNumber = request.getPaymentDetails().get("cardNumber");
            if (cardNumber != null && cardNumber.matches("^(0+|1+|2+)$")) {
                riskScore += 40;
                errors.add("Suspicious card number pattern detected");
            }
        }

        if (!request.isRecurring() && request.getAmount() != null
                && request.getAmount().compareTo(VERY_HIGH_RISK_AMOUNT) > 0) {
            riskScore += 15;
            warnings.add("First-time high-value transaction");
        }

        if (riskScore >= 70) {
            errors.add("Transaction blocked: Risk score too high (" + riskScore + ")");
        }

        boolean isValid = errors.isEmpty();
        log.info("Fraud detection result: {}, Risk score: {}", isValid ? "PASSED" : "FAILED", riskScore);

        return PaymentValidationResult.builder()
                .valid(isValid)
                .validationType("FRAUD_DETECTION")
                .errors(errors)
                .warnings(warnings)
                .riskScore(riskScore)
                .message(isValid
                        ? String.format("Fraud check passed with risk score: %d", riskScore)
                        : "Transaction blocked due to fraud detection")
                .build();
    }

    @Override
    public String getStrategyName() {
        return "FRAUD_DETECTION";
    }
}
