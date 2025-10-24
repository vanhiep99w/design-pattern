package com.designpatterns.showcase.strategy.payment;

import com.designpatterns.showcase.strategy.dto.PaymentValidationRequest;
import com.designpatterns.showcase.strategy.dto.PaymentValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FraudDetectionValidationStrategyTest {

    private FraudDetectionValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FraudDetectionValidationStrategy();
    }

    @Test
    void shouldPassLowRiskTransaction() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("50.00"))
                .paymentMethod("CREDIT_CARD")
                .ipAddress("203.0.113.1")
                .billingCountry("USA")
                .paymentDetails(details)
                .isRecurring(false)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertTrue(result.isValid());
        assertEquals("FRAUD_DETECTION", result.getValidationType());
        assertTrue(result.getRiskScore() < 70);
    }

    @Test
    void shouldWarnOnHighAmountTransaction() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("1500.00"))
                .paymentMethod("CREDIT_CARD")
                .ipAddress("203.0.113.1")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertTrue(result.isValid());
        assertTrue(result.getRiskScore() > 0);
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("High transaction")));
    }

    @Test
    void shouldFailOnVeryHighRiskTransaction() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "0000000000000000");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("6000.00"))
                .paymentMethod("CREDIT_CARD")
                .ipAddress("192.168.1.1")
                .billingCountry("XYZ")
                .paymentDetails(details)
                .isRecurring(false)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getRiskScore() >= 70);
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Risk score too high")));
    }

    @Test
    void shouldDetectSuspiciousIPAddress() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .ipAddress("192.168.1.1")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertTrue(result.getRiskScore() > 0);
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("Suspicious IP")));
    }

    @Test
    void shouldWarnWhenIPAddressIsMissing() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertTrue(result.getRiskScore() > 0);
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("IP address not provided")));
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("FRAUD_DETECTION", strategy.getStrategyName());
    }
}
