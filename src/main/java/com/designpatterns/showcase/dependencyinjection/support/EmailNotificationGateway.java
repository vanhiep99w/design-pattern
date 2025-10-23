package com.designpatterns.showcase.dependencyinjection.support;

import com.designpatterns.showcase.common.domain.User;
import com.designpatterns.showcase.dependencyinjection.config.OnboardingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationGateway implements NotificationGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationGateway.class);

    private final OnboardingProperties onboardingProperties;

    public EmailNotificationGateway(OnboardingProperties onboardingProperties) {
        this.onboardingProperties = onboardingProperties;
    }

    @Override
    public void sendWelcomeNotification(User user, String message) {
        LOGGER.info("Sending onboarding email to {} with support contact {}. Message: {}",
                user.getEmail(),
                onboardingProperties.getSupportEmail(),
                message);
    }
}
