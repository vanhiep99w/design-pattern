package com.designpatterns.showcase.dependencyinjection.service;

import com.designpatterns.showcase.common.domain.User;
import com.designpatterns.showcase.dependencyinjection.dto.UserRegistrationRequest;
import com.designpatterns.showcase.dependencyinjection.repository.OnboardingUserRepository;
import com.designpatterns.showcase.dependencyinjection.support.AuditTrailPublisher;
import com.designpatterns.showcase.dependencyinjection.support.NotificationGateway;
import com.designpatterns.showcase.dependencyinjection.support.UserOnboardingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class UserService {

    private final OnboardingUserRepository primaryUserRepository;
    private final OnboardingUserRepository fallbackUserRepository;
    private final AuditTrailPublisher auditTrailPublisher;

    @Autowired
    private OnboardingFeatureToggleService featureToggleService;

    private NotificationGateway notificationGateway;

    @Autowired
    public UserService(@Qualifier("jpaOnboardingUserRepository") OnboardingUserRepository primaryUserRepository,
                       @Qualifier("inMemoryOnboardingUserRepository") OnboardingUserRepository fallbackUserRepository,
                       AuditTrailPublisher auditTrailPublisher) {
        this.primaryUserRepository = primaryUserRepository;
        this.fallbackUserRepository = fallbackUserRepository;
        this.auditTrailPublisher = auditTrailPublisher;
    }

    @Autowired
    public void setNotificationGateway(NotificationGateway notificationGateway) {
        Assert.notNull(notificationGateway, "notificationGateway must not be null");
        this.notificationGateway = notificationGateway;
    }

    public User onboardUser(UserRegistrationRequest registrationRequest) {
        Assert.notNull(registrationRequest, "registrationRequest must not be null");
        Assert.hasText(registrationRequest.username(), "username must not be blank");
        Assert.hasText(registrationRequest.email(), "email must not be blank");
        Assert.hasText(registrationRequest.firstName(), "firstName must not be blank");
        Assert.hasText(registrationRequest.lastName(), "lastName must not be blank");

        OnboardingUserRepository repository = selectRepository();
        ensureUserDoesNotExist(registrationRequest, repository);

        User persistedUser = repository.save(registrationRequest.toDomainUser());
        auditTrailPublisher.recordUserOnboarding(persistedUser, repository == fallbackUserRepository);

        if (featureToggleService.isWelcomeNotificationEnabled()) {
            String message = featureToggleService.buildWelcomeMessage(persistedUser.getFirstName());
            notificationGateway.sendWelcomeNotification(persistedUser, message);
        }

        return persistedUser;
    }

    private void ensureUserDoesNotExist(UserRegistrationRequest registrationRequest, OnboardingUserRepository repository) {
        if (repository.existsByUsername(registrationRequest.username())) {
            throw new UserOnboardingException("Username already exists: " + registrationRequest.username());
        }
        if (repository.existsByEmail(registrationRequest.email())) {
            throw new UserOnboardingException("Email already exists: " + registrationRequest.email());
        }
    }

    private OnboardingUserRepository selectRepository() {
        return featureToggleService.shouldUseFallbackRepository() ? fallbackUserRepository : primaryUserRepository;
    }
}
