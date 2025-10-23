package com.designpatterns.showcase.dependencyinjection;

import com.designpatterns.showcase.common.domain.User;
import com.designpatterns.showcase.common.domain.UserRole;
import com.designpatterns.showcase.dependencyinjection.dto.UserRegistrationRequest;
import com.designpatterns.showcase.dependencyinjection.repository.OnboardingUserRepository;
import com.designpatterns.showcase.dependencyinjection.service.OnboardingFeatureToggleService;
import com.designpatterns.showcase.dependencyinjection.service.UserService;
import com.designpatterns.showcase.dependencyinjection.support.AuditTrailPublisher;
import com.designpatterns.showcase.dependencyinjection.support.NotificationGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "onboarding.notifications-enabled=false",
        "onboarding.fallback-enabled=false",
        "onboarding.welcome-message=Notifications disabled for %s",
        "onboarding.support-email=toggle@test.io"
})
class UserServiceNotificationToggleTest {

    @Autowired
    private UserService userService;

    @MockBean(name = "jpaOnboardingUserRepository")
    private OnboardingUserRepository jpaRepository;

    @MockBean(name = "inMemoryOnboardingUserRepository")
    private OnboardingUserRepository inMemoryRepository;

    @MockBean
    private NotificationGateway notificationGateway;

    @MockBean
    private AuditTrailPublisher auditTrailPublisher;

    @SpyBean
    private OnboardingFeatureToggleService featureToggleService;

    @Test
    void skipsNotificationWhenFeatureDisabled() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "laura",
                "laura@example.com",
                "Laura",
                "Palmer",
                UserRole.USER
        );

        when(jpaRepository.existsByUsername("laura")).thenReturn(false);
        when(jpaRepository.existsByEmail("laura@example.com")).thenReturn(false);
        when(jpaRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0, User.class);
            user.setId(303L);
            return user;
        });

        userService.onboardUser(request);

        verify(featureToggleService).shouldUseFallbackRepository();
        verify(featureToggleService).isWelcomeNotificationEnabled();
        verify(notificationGateway, never()).sendWelcomeNotification(any(User.class), any(String.class));
        verify(auditTrailPublisher).recordUserOnboarding(any(User.class), eq(false));
        verify(inMemoryRepository, never()).save(any(User.class));
    }
}
