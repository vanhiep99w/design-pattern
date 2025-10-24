package com.designpatterns.showcase.factory;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class SmsNotificationService implements NotificationService {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );
    private static final int MAX_MESSAGE_LENGTH = 160;
    private static final int MAX_EXTENDED_MESSAGE_LENGTH = 1600;

    @Override
    public NotificationResult send(NotificationRequest request) {
        log.info("Sending SMS notification to: {}", request.getRecipient());

        if (!validateRecipient(request.getRecipient())) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Invalid phone number format")
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return NotificationResult.builder()
                    .success(false)
                    .message("SMS message cannot be empty")
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        int messageLength = request.getMessage().length();
        if (messageLength > MAX_EXTENDED_MESSAGE_LENGTH) {
            return NotificationResult.builder()
                    .success(false)
                    .message("SMS message exceeds maximum length of " + MAX_EXTENDED_MESSAGE_LENGTH)
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        int segmentCount = (int) Math.ceil((double) messageLength / MAX_MESSAGE_LENGTH);
        String notificationId = "SMS-" + UUID.randomUUID().toString();
        
        String resultMessage = String.format("SMS sent successfully to %s (segments: %d)", 
                request.getRecipient(), segmentCount);
        
        log.info("SMS notification sent successfully. Notification ID: {}, Segments: {}", 
                notificationId, segmentCount);

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
            log.warn("Phone number is null or empty");
            return false;
        }

        String cleaned = recipient.replaceAll("[\\s()-]", "");
        boolean isValid = PHONE_PATTERN.matcher(cleaned).matches();
        
        if (!isValid) {
            log.warn("Invalid phone number format: {}", recipient);
        }
        return isValid;
    }

    @Override
    public String getNotificationType() {
        return "SMS";
    }
}
