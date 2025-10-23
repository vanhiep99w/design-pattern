package com.designpatterns.showcase.dependencyinjection.service;

import com.designpatterns.showcase.dependencyinjection.config.OnboardingProperties;
import org.springframework.stereotype.Service;

@Service
public class OnboardingFeatureToggleService {

    private final OnboardingProperties onboardingProperties;

    public OnboardingFeatureToggleService(OnboardingProperties onboardingProperties) {
        this.onboardingProperties = onboardingProperties;
    }

    public boolean isWelcomeNotificationEnabled() {
        return onboardingProperties.isNotificationsEnabled();
    }

    public boolean shouldUseFallbackRepository() {
        return onboardingProperties.isFallbackEnabled();
    }

    public String buildWelcomeMessage(String firstName) {
        return String.format(onboardingProperties.getWelcomeMessage(), firstName);
    }

    public String getSupportEmail() {
        return onboardingProperties.getSupportEmail();
    }
}
