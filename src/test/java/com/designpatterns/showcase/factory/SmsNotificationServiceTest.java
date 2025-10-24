package com.designpatterns.showcase.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmsNotificationServiceTest {

    private SmsNotificationService service;

    @BeforeEach
    void setUp() {
        service = new SmsNotificationService();
    }

    @Test
    void shouldSendValidSmsNotification() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("SMS")
                .recipient("+1234567890")
                .message("Test SMS message")
                .build();

        NotificationResult result = service.send(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getNotificationId());
        assertTrue(result.getNotificationId().startsWith("SMS-"));
        assertEquals("SMS", result.getNotificationType());
        assertTrue(result.getMessage().contains("+1234567890"));
        assertTrue(result.getMessage().contains("segments"));
        assertNotNull(result.getTimestamp());
    }

    @Test
    void shouldCalculateCorrectSegmentCountForShortMessage() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("SMS")
                .recipient("+1234567890")
                .message("Short message")
                .build();

        NotificationResult result = service.send(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("segments: 1"));
    }

    @Test
    void shouldCalculateCorrectSegmentCountForLongMessage() {
        String longMessage = "a".repeat(300);
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("SMS")
                .recipient("+1234567890")
                .message(longMessage)
                .build();

        NotificationResult result = service.send(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("segments: 2"));
    }

    @Test
    void shouldRejectSmsWithInvalidPhoneNumber() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("SMS")
                .recipient("invalid-phone")
                .message("Test message")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid phone number format"));
    }

    @Test
    void shouldRejectSmsWithEmptyMessage() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("SMS")
                .recipient("+1234567890")
                .message("")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("message cannot be empty"));
    }

    @Test
    void shouldRejectSmsWithTooLongMessage() {
        String tooLongMessage = "a".repeat(1601);
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("SMS")
                .recipient("+1234567890")
                .message(tooLongMessage)
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("exceeds maximum length"));
    }

    @Test
    void shouldValidateCorrectPhoneNumbers() {
        assertTrue(service.validateRecipient("+1234567890"));
        assertTrue(service.validateRecipient("+12345678901234"));
        assertTrue(service.validateRecipient("+4412345678"));
        assertTrue(service.validateRecipient("+1-234-567-8901"));
        assertTrue(service.validateRecipient("+1 (234) 567-8901"));
    }

    @Test
    void shouldRejectInvalidPhoneNumbers() {
        assertFalse(service.validateRecipient("12345"));
        assertFalse(service.validateRecipient("invalid"));
        assertFalse(service.validateRecipient(""));
        assertFalse(service.validateRecipient(null));
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertEquals("SMS", service.getNotificationType());
    }
}
