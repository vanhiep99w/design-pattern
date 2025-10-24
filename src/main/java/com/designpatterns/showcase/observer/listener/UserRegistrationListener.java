package com.designpatterns.showcase.observer.listener;

import com.designpatterns.showcase.observer.events.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRegistrationListener {

    private final ApplicationEventPublisher eventPublisher;

    public UserRegistrationListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Order(1)
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("User registered: UserId={}, Username={}, Email={}", 
            event.getUserId(), event.getUsername(), event.getEmail());
        log.info("Thread: {}", Thread.currentThread().getName());
    }

    @EventListener
    @Order(2)
    @Async("eventTaskExecutor")
    public void sendWelcomeEmail(UserRegisteredEvent event) {
        log.info("Step 1 - Sending welcome email to {}", event.getEmail());
        log.info("Async Thread: {}", Thread.currentThread().getName());
        simulateEmailDelivery();
        log.info("Welcome email sent successfully to {}", event.getEmail());
        
        eventPublisher.publishEvent(new UserProfileCreationEvent(this, event.getUserId()));
    }

    @EventListener
    @Order(3)
    @Async("eventTaskExecutor")
    public void createUserProfile(UserProfileCreationEvent event) {
        log.info("Step 2 - Creating user profile for UserId={}", event.getUserId());
        log.info("Async Thread: {}", Thread.currentThread().getName());
        simulateProfileCreation();
        log.info("User profile created successfully for UserId={}", event.getUserId());
        
        eventPublisher.publishEvent(new ExternalSystemNotificationEvent(this, event.getUserId()));
    }

    @EventListener
    @Order(4)
    @Async("eventTaskExecutor")
    public void notifyExternalSystem(ExternalSystemNotificationEvent event) {
        log.info("Step 3 - Notifying external system for UserId={}", event.getUserId());
        log.info("Async Thread: {}", Thread.currentThread().getName());
        simulateExternalSystemNotification();
        log.info("External system notified successfully for UserId={}", event.getUserId());
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void setupDefaultPreferences(UserRegisteredEvent event) {
        log.info("Setting up default preferences for UserId={}", event.getUserId());
        log.info("Async Thread: {}", Thread.currentThread().getName());
        simulatePreferenceSetup();
        log.info("Default preferences set for UserId={}", event.getUserId());
    }

    private void simulateEmailDelivery() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email delivery interrupted", e);
        }
    }

    private void simulateProfileCreation() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Profile creation interrupted", e);
        }
    }

    private void simulateExternalSystemNotification() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("External system notification interrupted", e);
        }
    }

    private void simulatePreferenceSetup() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Preference setup interrupted", e);
        }
    }

    public static class UserProfileCreationEvent {
        private final Object source;
        private final Long userId;

        public UserProfileCreationEvent(Object source, Long userId) {
            this.source = source;
            this.userId = userId;
        }

        public Object getSource() {
            return source;
        }

        public Long getUserId() {
            return userId;
        }
    }

    public static class ExternalSystemNotificationEvent {
        private final Object source;
        private final Long userId;

        public ExternalSystemNotificationEvent(Object source, Long userId) {
            this.source = source;
            this.userId = userId;
        }

        public Object getSource() {
            return source;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
