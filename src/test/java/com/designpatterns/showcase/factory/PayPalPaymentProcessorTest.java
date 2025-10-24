package com.designpatterns.showcase.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PayPalPaymentProcessorTest {

    private PayPalPaymentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PayPalPaymentProcessor();
    }

    @Test
    void shouldProcessValidPayPalPayment() {
        Map<String, String> details = new HashMap<>();
        details.put("email", "john.doe@example.com");
        details.put("accountId", "ABCD1234567890XYZ");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("PAYPAL")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("PP-"));
        assertNotNull(result.getProcessedAmount());
        assertTrue(result.getProcessedAmount().compareTo(new BigDecimal("100.00")) < 0);
        assertEquals("PAYPAL", result.getPaymentType());
        assertTrue(result.getMessage().contains("Fee"));
    }

    @Test
    void shouldRejectPaymentWithInvalidEmail() {
        Map<String, String> details = new HashMap<>();
        details.put("email", "invalid-email");
        details.put("accountId", "ABCD1234567890XYZ");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("PAYPAL")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid PayPal account details"));
    }

    @Test
    void shouldRejectPaymentWithInvalidAccountId() {
        Map<String, String> details = new HashMap<>();
        details.put("email", "john.doe@example.com");
        details.put("accountId", "invalid");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("PAYPAL")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldRejectPaymentBelowMinimumAmount() {
        Map<String, String> details = new HashMap<>();
        details.put("email", "john.doe@example.com");
        details.put("accountId", "ABCD1234567890XYZ");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("PAYPAL")
                .amount(new BigDecimal("0.001"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("below minimum"));
    }

    @Test
    void shouldRejectPaymentAboveMaximumAmount() {
        Map<String, String> details = new HashMap<>();
        details.put("email", "john.doe@example.com");
        details.put("accountId", "ABCD1234567890XYZ");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("PAYPAL")
                .amount(new BigDecimal("150000.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("exceeds limit"));
    }

    @Test
    void shouldValidateCorrectPayPalDetails() {
        Map<String, String> details = new HashMap<>();
        details.put("email", "john.doe@example.com");
        details.put("accountId", "ABCD1234567890XYZ");

        PaymentRequest request = PaymentRequest.builder()
                .paymentDetails(details)
                .build();

        assertTrue(processor.validatePaymentDetails(request));
    }

    @Test
    void shouldReturnCorrectPaymentType() {
        assertEquals("PAYPAL", processor.getPaymentType());
    }
}
