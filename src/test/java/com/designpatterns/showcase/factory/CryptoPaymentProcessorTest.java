package com.designpatterns.showcase.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CryptoPaymentProcessorTest {

    private CryptoPaymentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CryptoPaymentProcessor();
    }

    @Test
    void shouldProcessValidBitcoinPayment() {
        Map<String, String> details = new HashMap<>();
        details.put("walletAddress", "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
        details.put("cryptoType", "BTC");
        details.put("signature", "valid-signature-here");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CRYPTOCURRENCY")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("CRYPTO-BTC-"));
        assertEquals(new BigDecimal("100.00"), result.getProcessedAmount());
        assertEquals("CRYPTOCURRENCY", result.getPaymentType());
        assertTrue(result.getMessage().contains("BTC"));
        assertTrue(result.getMessage().contains("Blockchain Hash"));
    }

    @Test
    void shouldProcessValidEthereumPayment() {
        Map<String, String> details = new HashMap<>();
        details.put("walletAddress", "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed");
        details.put("cryptoType", "ETH");
        details.put("signature", "valid-signature-here");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CRYPTOCURRENCY")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getTransactionId().startsWith("CRYPTO-ETH-"));
        assertTrue(result.getMessage().contains("ETH"));
    }

    @Test
    void shouldRejectPaymentWithUnsupportedCrypto() {
        Map<String, String> details = new HashMap<>();
        details.put("walletAddress", "some-address");
        details.put("cryptoType", "DOGE");
        details.put("signature", "valid-signature-here");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CRYPTOCURRENCY")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid cryptocurrency wallet details"));
    }

    @Test
    void shouldRejectPaymentWithInvalidBitcoinAddress() {
        Map<String, String> details = new HashMap<>();
        details.put("walletAddress", "invalid-bitcoin-address");
        details.put("cryptoType", "BTC");
        details.put("signature", "valid-signature-here");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CRYPTOCURRENCY")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldRejectPaymentWithInvalidEthereumAddress() {
        Map<String, String> details = new HashMap<>();
        details.put("walletAddress", "invalid-eth-address");
        details.put("cryptoType", "ETH");
        details.put("signature", "valid-signature-here");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CRYPTOCURRENCY")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldRejectPaymentWithMissingSignature() {
        Map<String, String> details = new HashMap<>();
        details.put("walletAddress", "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
        details.put("cryptoType", "BTC");
        details.put("signature", "");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CRYPTOCURRENCY")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldRejectPaymentBelowMinimum() {
        Map<String, String> details = new HashMap<>();
        details.put("walletAddress", "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
        details.put("cryptoType", "BTC");
        details.put("signature", "valid-signature-here");

        PaymentRequest request = PaymentRequest.builder()
                .paymentType("CRYPTOCURRENCY")
                .amount(new BigDecimal("5.00"))
                .currency("USD")
                .customerId("CUST001")
                .paymentDetails(details)
                .build();

        PaymentResult result = processor.processPayment(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("below minimum"));
    }

    @Test
    void shouldValidateCorrectBitcoinDetails() {
        Map<String, String> details = new HashMap<>();
        details.put("walletAddress", "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
        details.put("cryptoType", "BTC");
        details.put("signature", "valid-signature-here");

        PaymentRequest request = PaymentRequest.builder()
                .paymentDetails(details)
                .build();

        assertTrue(processor.validatePaymentDetails(request));
    }

    @Test
    void shouldValidateCorrectEthereumDetails() {
        Map<String, String> details = new HashMap<>();
        details.put("walletAddress", "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed");
        details.put("cryptoType", "ETH");
        details.put("signature", "valid-signature-here");

        PaymentRequest request = PaymentRequest.builder()
                .paymentDetails(details)
                .build();

        assertTrue(processor.validatePaymentDetails(request));
    }

    @Test
    void shouldReturnCorrectPaymentType() {
        assertEquals("CRYPTOCURRENCY", processor.getPaymentType());
    }
}
