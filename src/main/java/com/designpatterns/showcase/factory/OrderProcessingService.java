package com.designpatterns.showcase.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final PaymentProcessorFactory paymentProcessorFactory;
    private final NotificationFactory.NotificationServiceProvider notificationServiceProvider;

    public OrderProcessingResult processOrder(OrderProcessingRequest request) {
        log.info("Processing order for customer: {}", request.getCustomerId());

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .paymentType(request.getPaymentType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .customerId(request.getCustomerId())
                .paymentDetails(request.getPaymentDetails())
                .build();

        PaymentProcessor paymentProcessor = paymentProcessorFactory.getPaymentProcessor(request.getPaymentType());
        PaymentResult paymentResult = paymentProcessor.processPayment(paymentRequest);

        if (!paymentResult.isSuccess()) {
            log.warn("Payment failed for customer: {}. Reason: {}", request.getCustomerId(), paymentResult.getMessage());
            return OrderProcessingResult.builder()
                    .success(false)
                    .message("Order failed: " + paymentResult.getMessage())
                    .paymentResult(paymentResult)
                    .build();
        }

        log.info("Payment successful for customer: {}. Transaction ID: {}", 
                request.getCustomerId(), paymentResult.getTransactionId());

        NotificationResult notificationResult = sendNotification(request, paymentResult);

        return OrderProcessingResult.builder()
                .success(true)
                .message("Order processed successfully")
                .paymentResult(paymentResult)
                .notificationResult(notificationResult)
                .build();
    }

    private NotificationResult sendNotification(OrderProcessingRequest request, PaymentResult paymentResult) {
        String notificationType = request.getNotificationType();
        String recipient = request.getNotificationRecipient();

        if (notificationType == null || recipient == null) {
            log.info("No notification requested for order");
            return null;
        }

        try {
            NotificationService notificationService = notificationServiceProvider.getNotificationService(notificationType);
            
            String subject = "Order Confirmation";
            String message = String.format(
                    "Your order has been processed successfully. Transaction ID: %s. Amount: %s %s. Payment method: %s",
                    paymentResult.getTransactionId(),
                    paymentResult.getProcessedAmount(),
                    request.getCurrency(),
                    paymentResult.getPaymentType()
            );

            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .notificationType(notificationType)
                    .recipient(recipient)
                    .subject(subject)
                    .message(message)
                    .metadata(request.getNotificationMetadata())
                    .build();

            NotificationResult result = notificationService.send(notificationRequest);
            
            if (result.isSuccess()) {
                log.info("Notification sent successfully to customer: {}", request.getCustomerId());
            } else {
                log.warn("Notification failed for customer: {}. Reason: {}", 
                        request.getCustomerId(), result.getMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("Error sending notification for customer: {}", request.getCustomerId(), e);
            return NotificationResult.builder()
                    .success(false)
                    .message("Failed to send notification: " + e.getMessage())
                    .notificationType(notificationType)
                    .build();
        }
    }

    public Map<String, Boolean> getSupportedPaymentTypes() {
        Map<String, Boolean> supportedTypes = new HashMap<>();
        supportedTypes.put("CREDIT_CARD", paymentProcessorFactory.isPaymentTypeSupported("CREDIT_CARD"));
        supportedTypes.put("PAYPAL", paymentProcessorFactory.isPaymentTypeSupported("PAYPAL"));
        supportedTypes.put("CRYPTOCURRENCY", paymentProcessorFactory.isPaymentTypeSupported("CRYPTOCURRENCY"));
        return supportedTypes;
    }

    public Map<String, Boolean> getSupportedNotificationTypes() {
        Map<String, Boolean> supportedTypes = new HashMap<>();
        supportedTypes.put("EMAIL", notificationServiceProvider.isNotificationTypeSupported("EMAIL"));
        supportedTypes.put("SMS", notificationServiceProvider.isNotificationTypeSupported("SMS"));
        supportedTypes.put("PUSH", notificationServiceProvider.isNotificationTypeSupported("PUSH"));
        return supportedTypes;
    }
}
