package com.designpatterns.showcase.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreditCardPaymentProcessorTest {

    private CreditCardPaymentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CreditCardPaymentProcessor();
    }

    @Test
    void shouldProcessValidCreditCardPayment() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4532015112830366");
        details.put("cvv", "123");
        details.put("expiryDate", "12/25");
        details.put("cardHolderName", "John Doe");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CREDIT_CARD")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("CC-"));
        assertEquals(new BigDecimal("100.00"), result.getProcessedAmount());
        assertEquals("CREDIT_CARD", result.getPaymentType());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void shouldRejectPaymentWithInvalidCardNumber() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "1234567890123456");
        details.put("cvv", "123");
        details.put("expiryDate", "12/25");
        details.put("cardHolderName", "John Doe");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CREDIT_CARD")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid credit card details"));
    }

    @Test
    void shouldRejectPaymentWithInvalidCVV() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4532015112830366");
        details.put("cvv", "12");
        details.put("expiryDate", "12/25");
        details.put("cardHolderName", "John Doe");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CREDIT_CARD")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldRejectPaymentWithExpiredCard() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4532015112830366");
        details.put("cvv", "123");
        details.put("expiryDate", "01/20");
        details.put("cardHolderName", "John Doe");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CREDIT_CARD")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldRejectPaymentWithMissingCardHolderName() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4532015112830366");
        details.put("cvv", "123");
        details.put("expiryDate", "12/25");
        details.put("cardHolderName", "");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CREDIT_CARD")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldRejectPaymentExceedingMaxAmount() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4532015112830366");
        details.put("cvv", "123");
        details.put("expiryDate", "12/25");
        details.put("cardHolderName", "John Doe");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CREDIT_CARD")
                .amount(new BigDecimal("60000.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("exceeds limit"));
    }

    @Test
    void shouldValidateCorrectCardDetails() {
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "4532015112830366");
        details.put("cvv", "123");
        details.put("expiryDate", "12/25");
        details.put("cardHolderName", "John Doe");

        PaymentRequest request = PaymentRequest.builder()
                .paymentDetails(details)
                .build();

        assertTrue(processor.validatePaymentDetails(request));
    }

    @Test
    void shouldReturnCorrectPaymentType() {
        assertEquals("CREDIT_CARD", processor.getPaymentType());
    }
}
