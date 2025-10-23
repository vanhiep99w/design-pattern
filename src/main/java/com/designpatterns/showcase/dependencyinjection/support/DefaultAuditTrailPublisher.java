package com.designpatterns.showcase.dependencyinjection.support;

import com.designpatterns.showcase.common.domain.User;
import com.designpatterns.showcase.dependencyinjection.config.OnboardingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultAuditTrailPublisher implements AuditTrailPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuditTrailPublisher.class);

    private final OnboardingProperties onboardingProperties;

    public DefaultAuditTrailPublisher(OnboardingProperties onboardingProperties) {
        this.onboardingProperties = onboardingProperties;
    }

    @Override
    public void recordUserOnboarding(User user, boolean fallbackRepositoryUsed) {
        LOGGER.info("User [{}] onboarded via {} repository. Contact support at {} for follow up.",
                user.getUsername(),
                fallbackRepositoryUsed ? "in-memory" : "JPA",
                onboardingProperties.getSupportEmail());
    }
}
