package com.designpatterns.showcase.factory;

public interface NotificationService {
    NotificationResult send(NotificationRequest request);
    boolean validateRecipient(String recipient);
    String getNotificationType();
}
