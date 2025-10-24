package com.designpatterns.showcase.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailNotificationServiceTest {

    private EmailNotificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailNotificationService();
    }

    @Test
    void shouldSendValidEmailNotification() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("EMAIL")
                .recipient("test@example.com")
                .subject("Test Subject")
                .message("Test message content")
                .build();

        NotificationResult result = service.send(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getNotificationId());
        assertTrue(result.getNotificationId().startsWith("EMAIL-"));
        assertEquals("EMAIL", result.getNotificationType());
        assertTrue(result.getMessage().contains("test@example.com"));
        assertNotNull(result.getTimestamp());
    }

    @Test
    void shouldRejectEmailWithInvalidRecipient() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("EMAIL")
                .recipient("invalid-email")
                .subject("Test Subject")
                .message("Test message")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid email address"));
    }

    @Test
    void shouldRejectEmailWithEmptySubject() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("EMAIL")
                .recipient("test@example.com")
                .subject("")
                .message("Test message")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("subject cannot be empty"));
    }

    @Test
    void shouldRejectEmailWithEmptyMessage() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("EMAIL")
                .recipient("test@example.com")
                .subject("Test Subject")
                .message("")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("message cannot be empty"));
    }

    @Test
    void shouldRejectEmailWithTooLongSubject() {
        String longSubject = "a".repeat(201);
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("EMAIL")
                .recipient("test@example.com")
                .subject(longSubject)
                .message("Test message")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("exceeds maximum length"));
    }

    @Test
    void shouldRejectEmailWithTooLongMessage() {
        String longMessage = "a".repeat(10001);
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("EMAIL")
                .recipient("test@example.com")
                .subject("Test Subject")
                .message(longMessage)
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("exceeds maximum length"));
    }

    @Test
    void shouldValidateCorrectEmailAddress() {
        assertTrue(service.validateRecipient("test@example.com"));
        assertTrue(service.validateRecipient("user.name@example.co.uk"));
        assertTrue(service.validateRecipient("user+tag@example.com"));
    }

    @Test
    void shouldRejectInvalidEmailAddress() {
        assertFalse(service.validateRecipient("invalid"));
        assertFalse(service.validateRecipient("@example.com"));
        assertFalse(service.validateRecipient("test@"));
        assertFalse(service.validateRecipient(""));
        assertFalse(service.validateRecipient(null));
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertEquals("EMAIL", service.getNotificationType());
    }
}
