package com.designpatterns.showcase.dependencyinjection;

import com.designpatterns.showcase.common.domain.User;
import com.designpatterns.showcase.common.domain.UserRole;
import com.designpatterns.showcase.dependencyinjection.dto.UserRegistrationRequest;
import com.designpatterns.showcase.dependencyinjection.repository.OnboardingUserRepository;
import com.designpatterns.showcase.dependencyinjection.service.UserService;
import com.designpatterns.showcase.dependencyinjection.support.AuditTrailPublisher;
import com.designpatterns.showcase.dependencyinjection.support.NotificationGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "onboarding.notifications-enabled=true",
        "onboarding.fallback-enabled=false",
        "onboarding.welcome-message=Welcome, %s!",
        "onboarding.support-email=support@test.io"
})
class UserServiceConstructorInjectionTest {

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

    @Test
    void usesPrimaryRepositoryWhenFallbackDisabled() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "jane",
                "jane@example.com",
                "Jane",
                "Doe",
                UserRole.ADMIN
        );

        when(jpaRepository.existsByUsername("jane")).thenReturn(false);
        when(jpaRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(jpaRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0, User.class);
            user.setId(101L);
            return user;
        });

        User saved = userService.onboardUser(request);

        assertEquals(101L, saved.getId());
        verify(jpaRepository).save(any(User.class));
        verify(inMemoryRepository, never()).save(any(User.class));
        verify(notificationGateway).sendWelcomeNotification(eq(saved), contains("Jane"));
        verify(auditTrailPublisher).recordUserOnboarding(eq(saved), eq(false));
    }
}
