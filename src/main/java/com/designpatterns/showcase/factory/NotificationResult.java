package com.designpatterns.showcase.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResult {
    private boolean success;
    private String notificationId;
    private String message;
    private String notificationType;
    private LocalDateTime timestamp;
}
