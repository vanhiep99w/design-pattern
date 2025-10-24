package com.designpatterns.showcase.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class NotificationFactory {

    @Bean
    public EmailNotificationService emailNotificationService() {
        log.info("Creating EmailNotificationService bean");
        return new EmailNotificationService();
    }

    @Bean
    public SmsNotificationService smsNotificationService() {
        log.info("Creating SmsNotificationService bean");
        return new SmsNotificationService();
    }

    @Bean
    public PushNotificationService pushNotificationService() {
        log.info("Creating PushNotificationService bean");
        return new PushNotificationService();
    }

    @Bean
    public NotificationServiceProvider notificationServiceProvider(List<NotificationService> services) {
        log.info("Creating NotificationServiceProvider with {} services", services.size());
        return new NotificationServiceProvider(services);
    }

    public static class NotificationServiceProvider {
        private final Map<String, NotificationService> services = new HashMap<>();

        public NotificationServiceProvider(List<NotificationService> notificationServices) {
            for (NotificationService service : notificationServices) {
                services.put(service.getNotificationType(), service);
                log.info("Registered notification service: {}", service.getNotificationType());
            }
        }

        public NotificationService getNotificationService(String notificationType) {
            if (notificationType == null) {
                throw new IllegalArgumentException("Notification type cannot be null");
            }

            String normalizedType = notificationType.toUpperCase().trim();
            NotificationService service = services.get(normalizedType);

            if (service == null) {
                throw new UnsupportedNotificationTypeException("Unsupported notification type: " + notificationType);
            }

            log.debug("Retrieved notification service for type: {}", normalizedType);
            return service;
        }

        public boolean isNotificationTypeSupported(String notificationType) {
            if (notificationType == null) {
                return false;
            }
            return services.containsKey(notificationType.toUpperCase().trim());
        }

        public Map<String, NotificationService> getAllServices() {
            return new HashMap<>(services);
        }
    }
}
