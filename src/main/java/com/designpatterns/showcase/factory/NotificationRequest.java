package com.designpatterns.showcase.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String notificationType;
    private String recipient;
    private String subject;
    private String message;
    private Map<String, String> metadata;
}
