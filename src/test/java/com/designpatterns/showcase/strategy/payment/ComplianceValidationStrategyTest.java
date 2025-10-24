package com.designpatterns.showcase.strategy.payment;

import com.designpatterns.showcase.strategy.dto.PaymentValidationRequest;
import com.designpatterns.showcase.strategy.dto.PaymentValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComplianceValidationStrategyTest {

    private ComplianceValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ComplianceValidationStrategy();
    }

    @Test
    void shouldPassCompliantTransaction() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("500.00"))
                .paymentMethod("CREDIT_CARD")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertTrue(result.isValid());
        assertEquals("COMPLIANCE", result.getValidationType());
    }

    @Test
    void shouldFailOnSanctionedCountry() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .billingCountry("SANCTIONED1")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("sanctions")));
    }

    @Test
    void shouldFailOnRestrictedPaymentMethod() {
        Map<String, String> details = new HashMap<>();
        details.put("wallet", "0x123456789");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CRYPTO")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("restricted")));
    }

    @Test
    void shouldWarnOnAMLThreshold() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("10000.00"))
                .paymentMethod("CREDIT_CARD")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertTrue(result.isValid());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("AML")));
    }

    @Test
    void shouldFailWhenCustomerIdMissingForHighAmount() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .amount(new BigDecimal("10000.00"))
                .paymentMethod("CREDIT_CARD")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Customer identification")));
    }

    @Test
    void shouldWarnOnReportingThreshold() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4111111111111111");

        PaymentValidationRequest request = PaymentValidationRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("3500.00"))
                .paymentMethod("CREDIT_CARD")
                .billingCountry("USA")
                .paymentDetails(details)
                .build();

        PaymentValidationResult result = strategy.validate(request);

        assertTrue(result.isValid());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("reported to regulatory")));
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("COMPLIANCE", strategy.getStrategyName());
    }
}
