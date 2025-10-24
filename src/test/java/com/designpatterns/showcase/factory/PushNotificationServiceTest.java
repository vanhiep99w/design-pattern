package com.designpatterns.showcase.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PushNotificationServiceTest {

    private PushNotificationService service;

    @BeforeEach
    void setUp() {
        service = new PushNotificationService();
    }

    @Test
    void shouldSendValidPushNotification() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("platform", "iOS");

        NotificationRequest request = NotificationRequest.builder()
                .notificationType("PUSH")
                .recipient("a".repeat(64))
                .subject("Test Title")
                .message("Test push notification message")
                .metadata(metadata)
                .build();

        NotificationResult result = service.send(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getNotificationId());
        assertTrue(result.getNotificationId().startsWith("PUSH-"));
        assertEquals("PUSH", result.getNotificationType());
        assertTrue(result.getMessage().contains("iOS"));
        assertNotNull(result.getTimestamp());
    }

    @Test
    void shouldDetectAndroidPlatform() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("platform", "Android");

        NotificationRequest request = NotificationRequest.builder()
                .notificationType("PUSH")
                .recipient("a".repeat(64))
                .subject("Test Title")
                .message("Test message")
                .metadata(metadata)
                .build();

        NotificationResult result = service.send(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Android"));
    }

    @Test
    void shouldHandleUnknownPlatform() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("PUSH")
                .recipient("a".repeat(64))
                .subject("Test Title")
                .message("Test message")
                .build();

        NotificationResult result = service.send(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Unknown"));
    }

    @Test
    void shouldRejectPushWithInvalidDeviceToken() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("PUSH")
                .recipient("invalid-token")
                .subject("Test Title")
                .message("Test message")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid device token format"));
    }

    @Test
    void shouldRejectPushWithEmptyTitle() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("PUSH")
                .recipient("a".repeat(64))
                .subject("")
                .message("Test message")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("title cannot be empty"));
    }

    @Test
    void shouldRejectPushWithEmptyMessage() {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("PUSH")
                .recipient("a".repeat(64))
                .subject("Test Title")
                .message("")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("message cannot be empty"));
    }

    @Test
    void shouldRejectPushWithTooLongTitle() {
        String longTitle = "a".repeat(66);
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("PUSH")
                .recipient("a".repeat(64))
                .subject(longTitle)
                .message("Test message")
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("title exceeds maximum length"));
    }

    @Test
    void shouldRejectPushWithTooLongMessage() {
        String longMessage = "a".repeat(241);
        NotificationRequest request = NotificationRequest.builder()
                .notificationType("PUSH")
                .recipient("a".repeat(64))
                .subject("Test Title")
                .message(longMessage)
                .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("message exceeds maximum length"));
    }

    @Test
    void shouldValidateCorrectDeviceTokens() {
        assertTrue(service.validateRecipient("a".repeat(64)));
        assertTrue(service.validateRecipient("A".repeat(64)));
        assertTrue(service.validateRecipient("0123456789abcdef".repeat(4)));
        assertTrue(service.validateRecipient("a_b-c".repeat(30)));
    }

    @Test
    void shouldRejectInvalidDeviceTokens() {
        assertFalse(service.validateRecipient("short"));
        assertFalse(service.validateRecipient(""));
        assertFalse(service.validateRecipient(null));
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertEquals("PUSH", service.getNotificationType());
    }
}
