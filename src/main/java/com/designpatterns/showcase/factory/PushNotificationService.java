package com.designpatterns.showcase.factory;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class PushNotificationService implements NotificationService {

    private static final Pattern DEVICE_TOKEN_PATTERN = Pattern.compile(
            "^[a-fA-F0-9]{64}$|^[a-zA-Z0-9_-]{100,200}$"
    );
    private static final int MAX_TITLE_LENGTH = 65;
    private static final int MAX_MESSAGE_LENGTH = 240;

    @Override
    public NotificationResult send(NotificationRequest request) {
        log.info("Sending push notification to device: {}", request.getRecipient());

        if (!validateRecipient(request.getRecipient())) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Invalid device token format")
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Push notification title cannot be empty")
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getSubject().length() > MAX_TITLE_LENGTH) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Push notification title exceeds maximum length of " + MAX_TITLE_LENGTH)
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Push notification message cannot be empty")
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getMessage().length() > MAX_MESSAGE_LENGTH) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Push notification message exceeds maximum length of " + MAX_MESSAGE_LENGTH)
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        String platform = determinePlatform(request.getMetadata());
        String notificationId = "PUSH-" + UUID.randomUUID().toString();
        
        String resultMessage = String.format("Push notification sent successfully to %s device", platform);
        
        log.info("Push notification sent successfully. Notification ID: {}, Platform: {}", 
                notificationId, platform);

        return NotificationResult.builder()
                .success(true)
                .notificationId(notificationId)
                .message(resultMessage)
                .notificationType(getNotificationType())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean validateRecipient(String recipient) {
        if (recipient == null || recipient.trim().isEmpty()) {
            log.warn("Device token is null or empty");
            return false;
        }

        boolean isValid = DEVICE_TOKEN_PATTERN.matcher(recipient).matches();
        if (!isValid) {
            log.warn("Invalid device token format");
        }
        return isValid;
    }

    @Override
    public String getNotificationType() {
        return "PUSH";
    }

    private String determinePlatform(Map<String, String> metadata) {
        if (metadata == null) {
            return "Unknown";
        }
        
        String platform = metadata.get("platform");
        if (platform != null && (platform.equalsIgnoreCase("iOS") || platform.equalsIgnoreCase("Android"))) {
            return platform;
        }
        return "Unknown";
    }
}
