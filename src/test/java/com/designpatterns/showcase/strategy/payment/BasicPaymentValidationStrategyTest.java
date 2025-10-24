package com.designpatterns.showcase.strategy.payment;

import com.designpatterns.showcase.strategy.dto.PaymentValidationRequest;
import com.designpatterns.showcase.strategy.dto.PaymentValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BasicPaymentValidationStrategyTest {

    private BasicPaymentValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BasicPaymentValidationStrategy();
    }

    @Test
    void shouldPassBasicValidation() {
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

        assertTrue(result.isValid());
        assertEquals("BASIC", result.getValidationType());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void shouldFailWhenAmountIsNull() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .paymentMethod("CREDIT_CARD")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("amount is required")));
    }

    @Test
    void shouldFailWhenAmountBelowMinimum() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("0.00"))
                .paymentMethod("CREDIT_CARD")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("at least")));
    }

    @Test
    void shouldFailWhenAmountExceedsMaximum() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("15000.00"))
                .paymentMethod("CREDIT_CARD")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("exceeds maximum")));
    }

    @Test
    void shouldFailWhenCustomerIdIsNull() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Customer ID")));
    }

    @Test
    void shouldFailWhenPaymentMethodIsNull() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Payment method")));
    }

    @Test
    void shouldFailWhenPaymentDetailsAreNull() {
        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Payment details")));
    }

    @Test
    void shouldWarnWhenBillingCountryIsMissing() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertTrue(result.isValid());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("country")));
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("BASIC", strategy.getStrategyName());
    }
}
