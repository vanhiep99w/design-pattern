package com.designpatterns.showcase.strategy.context;

import com.designpatterns.showcase.strategy.dto.PaymentValidationRequest;
import com.designpatterns.showcase.strategy.dto.PaymentValidationResult;
import com.designpatterns.showcase.strategy.exception.UnsupportedStrategyException;
import com.designpatterns.showcase.strategy.payment.PaymentValidationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PaymentValidationContext {

    private final Map<String, PaymentValidationStrategy> strategies;

    public PaymentValidationContext(
            @Qualifier("basicPaymentValidation") PaymentValidationStrategy basicValidation,
            @Qualifier("fraudDetectionValidation") PaymentValidationStrategy fraudValidation,
            @Qualifier("complianceValidation") PaymentValidationStrategy complianceValidation) {
        this.strategies = Map.of(
                "BASIC", basicValidation,
                "FRAUD_DETECTION", fraudValidation,
                "COMPLIANCE", complianceValidation
        );
    }

    public PaymentValidationResult validatePayment(String strategyType, PaymentValidationRequest request) {
        log.info("Selecting payment validation strategy: {}", strategyType);

        PaymentValidationStrategy strategy = strategies.get(strategyType.toUpperCase());
        if (strategy == null) {
            throw new UnsupportedStrategyException("Unknown validation strategy: " + strategyType);
        }

        return strategy.validate(request);
    }

    public PaymentValidationResult validatePaymentWithAllStrategies(PaymentValidationRequest request) {
        log.info("Validating payment with all strategies for customer: {}", request.getCustomerId());

        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();
        int totalRiskScore = 0;
        boolean allValid = true;

        for (Map.Entry<String, PaymentValidationStrategy> entry : strategies.entrySet()) {
            PaymentValidationResult result = entry.getValue().validate(request);

            if (!result.isValid()) {
                allValid = false;
            }

            if (result.getErrors() != null) {
                allErrors.addAll(result.getErrors());
            }

            if (result.getWarnings() != null) {
                allWarnings.addAll(result.getWarnings());
            }

            if (result.getRiskScore() != null) {
                totalRiskScore += result.getRiskScore();
            }

            log.info("Strategy {} result: {}", entry.getKey(), result.isValid() ? "PASSED" : "FAILED");
        }

        return PaymentValidationResult.builder()
                .valid(allValid)
                .validationType("COMPOSITE")
                .errors(allErrors)
                .warnings(allWarnings)
                .riskScore(totalRiskScore)
                .message(allValid ? "All validations passed" : "One or more validations failed")
                .build();
    }
}
