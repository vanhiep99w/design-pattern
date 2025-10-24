package com.designpatterns.showcase.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {

    @Mock
    private PaymentProcessorFactory paymentProcessorFactory;

    @Mock
    private NotificationFactory.NotificationServiceProvider notificationServiceProvider;

    @Mock
    private PaymentProcessor paymentProcessor;

    @Mock
    private NotificationService notificationService;

    private OrderProcessingService service;

    @BeforeEach
    void setUp() {
        service = new OrderProcessingService(paymentProcessorFactory, notificationServiceProvider);
    }

    @Test
    void shouldProcessOrderSuccessfully() {
        Map<String, String> paymentDetails = new HashMap<>();
        paymentDetails.put("cardNumber", "4532015112830366");
        paymentDetails.put("cvv", "123");
        paymentDetails.put("expiryDate", "12/25");
        paymentDetails.put("cardHolderName", "John Doe");

        OrderProcessingRequest request = OrderProcessingRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentType("CREDIT_CARD")
                .paymentDetails(paymentDetails)
                .notificationType("EMAIL")
                .notificationRecipient("test@example.com")
                .build();

        PaymentResult paymentResult = PaymentResult.builder()
                .success(true)
                .transactionId("TXN123")
                .processedAmount(new BigDecimal("100.00"))
                .paymentType("CREDIT_CARD")
                .timestamp(LocalDateTime.now())
                .build();

        NotificationResult notificationResult = NotificationResult.builder()
                .success(true)
                .notificationId("NOTIF123")
                .notificationType("EMAIL")
                .timestamp(LocalDateTime.now())
                .build();

        when(paymentProcessorFactory.getPaymentProcessor("CREDIT_CARD")).thenReturn(paymentProcessor);
        when(paymentProcessor.processPayment(any(PaymentRequest.class))).thenReturn(paymentResult);
        when(notificationServiceProvider.getNotificationService("EMAIL")).thenReturn(notificationService);
        when(notificationService.send(any(NotificationRequest.class))).thenReturn(notificationResult);

        OrderProcessingResult result = service.processOrder(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getPaymentResult());
        assertTrue(result.getPaymentResult().isSuccess());
        assertNotNull(result.getNotificationResult());
        assertTrue(result.getNotificationResult().isSuccess());

        verify(paymentProcessorFactory).getPaymentProcessor("CREDIT_CARD");
        verify(paymentProcessor).processPayment(any(PaymentRequest.class));
        verify(notificationServiceProvider).getNotificationService("EMAIL");
        verify(notificationService).send(any(NotificationRequest.class));
    }

    @Test
    void shouldHandlePaymentFailure() {
        Map<String, String> paymentDetails = new HashMap<>();
        paymentDetails.put("cardNumber", "invalid");

        OrderProcessingRequest request = OrderProcessingRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentType("CREDIT_CARD")
                .paymentDetails(paymentDetails)
                .build();

        PaymentResult paymentResult = PaymentResult.builder()
                .success(false)
                .message("Invalid card details")
                .paymentType("CREDIT_CARD")
                .timestamp(LocalDateTime.now())
                .build();

        when(paymentProcessorFactory.getPaymentProcessor("CREDIT_CARD")).thenReturn(paymentProcessor);
        when(paymentProcessor.processPayment(any(PaymentRequest.class))).thenReturn(paymentResult);

        OrderProcessingResult result = service.processOrder(request);

        assertFalse(result.isSuccess());
        assertNotNull(result.getPaymentResult());
        assertFalse(result.getPaymentResult().isSuccess());
        assertNull(result.getNotificationResult());

        verify(paymentProcessorFactory).getPaymentProcessor("CREDIT_CARD");
        verify(paymentProcessor).processPayment(any(PaymentRequest.class));
        verify(notificationServiceProvider, never()).getNotificationService(anyString());
    }

    @Test
    void shouldProcessOrderWithoutNotification() {
        Map<String, String> paymentDetails = new HashMap<>();
        paymentDetails.put("cardNumber", "4532015112830366");

        OrderProcessingRequest request = OrderProcessingRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentType("CREDIT_CARD")
                .paymentDetails(paymentDetails)
                .build();

        PaymentResult paymentResult = PaymentResult.builder()
                .success(true)
                .transactionId("TXN123")
                .processedAmount(new BigDecimal("100.00"))
                .paymentType("CREDIT_CARD")
                .timestamp(LocalDateTime.now())
                .build();

        when(paymentProcessorFactory.getPaymentProcessor("CREDIT_CARD")).thenReturn(paymentProcessor);
        when(paymentProcessor.processPayment(any(PaymentRequest.class))).thenReturn(paymentResult);

        OrderProcessingResult result = service.processOrder(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getPaymentResult());
        assertNull(result.getNotificationResult());

        verify(notificationServiceProvider, never()).getNotificationService(anyString());
    }

    @Test
    void shouldHandleNotificationFailure() {
        Map<String, String> paymentDetails = new HashMap<>();
        paymentDetails.put("cardNumber", "4532015112830366");

        OrderProcessingRequest request = OrderProcessingRequest.builder()
                .customerId("CUST001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentType("CREDIT_CARD")
                .paymentDetails(paymentDetails)
                .notificationType("EMAIL")
                .notificationRecipient("invalid-email")
                .build();

        PaymentResult paymentResult = PaymentResult.builder()
                .success(true)
                .transactionId("TXN123")
                .processedAmount(new BigDecimal("100.00"))
                .paymentType("CREDIT_CARD")
                .timestamp(LocalDateTime.now())
                .build();

        NotificationResult notificationResult = NotificationResult.builder()
                .success(false)
                .message("Invalid email")
                .notificationType("EMAIL")
                .timestamp(LocalDateTime.now())
                .build();

        when(paymentProcessorFactory.getPaymentProcessor("CREDIT_CARD")).thenReturn(paymentProcessor);
        when(paymentProcessor.processPayment(any(PaymentRequest.class))).thenReturn(paymentResult);
        when(notificationServiceProvider.getNotificationService("EMAIL")).thenReturn(notificationService);
        when(notificationService.send(any(NotificationRequest.class))).thenReturn(notificationResult);

        OrderProcessingResult result = service.processOrder(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getPaymentResult());
        assertTrue(result.getPaymentResult().isSuccess());
        assertNotNull(result.getNotificationResult());
        assertFalse(result.getNotificationResult().isSuccess());
    }

    @Test
    void shouldReturnSupportedPaymentTypes() {
        when(paymentProcessorFactory.isPaymentTypeSupported("CREDIT_CARD")).thenReturn(true);
        when(paymentProcessorFactory.isPaymentTypeSupported("PAYPAL")).thenReturn(true);
        when(paymentProcessorFactory.isPaymentTypeSupported("CRYPTOCURRENCY")).thenReturn(true);

        Map<String, Boolean> supportedTypes = service.getSupportedPaymentTypes();

        assertEquals(3, supportedTypes.size());
        assertTrue(supportedTypes.get("CREDIT_CARD"));
        assertTrue(supportedTypes.get("PAYPAL"));
        assertTrue(supportedTypes.get("CRYPTOCURRENCY"));
    }

    @Test
    void shouldReturnSupportedNotificationTypes() {
        when(notificationServiceProvider.isNotificationTypeSupported("EMAIL")).thenReturn(true);
        when(notificationServiceProvider.isNotificationTypeSupported("SMS")).thenReturn(true);
        when(notificationServiceProvider.isNotificationTypeSupported("PUSH")).thenReturn(true);

        Map<String, Boolean> supportedTypes = service.getSupportedNotificationTypes();

        assertEquals(3, supportedTypes.size());
        assertTrue(supportedTypes.get("EMAIL"));
        assertTrue(supportedTypes.get("SMS"));
        assertTrue(supportedTypes.get("PUSH"));
    }
}
