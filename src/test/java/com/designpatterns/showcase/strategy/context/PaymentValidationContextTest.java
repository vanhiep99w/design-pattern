package com.designpatterns.showcase.strategy.context;

import com.designpatterns.showcase.strategy.dto.PaymentValidationRequest;
import com.designpatterns.showcase.strategy.dto.PaymentValidationResult;
import com.designpatterns.showcase.strategy.exception.UnsupportedStrategyException;
import com.designpatterns.showcase.strategy.payment.BasicPaymentValidationStrategy;
import com.designpatterns.showcase.strategy.payment.ComplianceValidationStrategy;
import com.designpatterns.showcase.strategy.payment.FraudDetectionValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaymentValidationContextTest {

    private PaymentValidationContext context;

    @BeforeEach
    void setUp() {
        context = new PaymentValidationContext(
                new BasicPaymentValidationStrategy(),
                new FraudDetectionValidationStrategy(),
                new ComplianceValidationStrategy()
        );
    }

    @Test
    void shouldValidateWithBasicStrategy() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = context.validatePayment("BASIC", request);

        assertTrue(result.isValid());
        assertEquals("BASIC", result.getValidationType());
    }

    @Test
    void shouldValidateWithFraudDetection() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .ipAddress("203.0.113.1")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = context.validatePayment("FRAUD_DETECTION", request);

        assertTrue(result.isValid());
        assertEquals("FRAUD_DETECTION", result.getValidationType());
    }

    @Test
    void shouldValidateWithCompliance() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = context.validatePayment("COMPLIANCE", request);

        assertTrue(result.isValid());
        assertEquals("COMPLIANCE", result.getValidationType());
    }

    @Test
    void shouldThrowExceptionForUnknownStrategy() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .paymentDetails(details)
                .build();

        assertThrows(UnsupportedStrategyException.class, () -> {
            context.validatePayment("UNKNOWN", request);
        });
    }

    @Test
    void shouldValidateWithAllStrategies() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .ipAddress("203.0.113.1")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = context.validatePaymentWithAllStrategies(request);

        assertTrue(result.isValid());
        assertEquals("COMPOSITE", result.getValidationType());
    }

    @Test
    void shouldFailCompositeWhenAnyStrategyFails() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CRYPTO")
                .ipAddress("203.0.113.1")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = context.validatePaymentWithAllStrategies(request);

        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
    }
}
