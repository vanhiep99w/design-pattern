package com.designpatterns.showcase.factory;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class EmailNotificationService implements NotificationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final int MAX_SUBJECT_LENGTH = 200;
    private static final int MAX_MESSAGE_LENGTH = 10000;

    @Override
    public NotificationResult send(NotificationRequest request) {
        log.info("Sending email notification to: {}", request.getRecipient());

        if (!validateRecipient(request.getRecipient())) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Invalid email address")
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Email subject cannot be empty")
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getSubject().length() > MAX_SUBJECT_LENGTH) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Email subject exceeds maximum length of " + MAX_SUBJECT_LENGTH)
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Email message cannot be empty")
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getMessage().length() > MAX_MESSAGE_LENGTH) {
            return NotificationResult.builder()
                    .success(false)
                    .message("Email message exceeds maximum length of " + MAX_MESSAGE_LENGTH)
                    .notificationType(getNotificationType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        String notificationId = "EMAIL-" + UUID.randomUUID().toString();
        log.info("Email notification sent successfully. Notification ID: {}", notificationId);

        return NotificationResult.builder()
                .success(true)
                .notificationId(notificationId)
                .message("Email sent successfully to " + request.getRecipient())
                .notificationType(getNotificationType())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean validateRecipient(String recipient) {
        if (recipient == null || recipient.trim().isEmpty()) {
            log.warn("Email recipient is null or empty");
            return false;
        }

        boolean isValid = EMAIL_PATTERN.matcher(recipient).matches();
        if (!isValid) {
            log.warn("Invalid email format: {}", recipient);
        }
        return isValid;
    }

    @Override
    public String getNotificationType() {
        return "EMAIL";
    }
}
