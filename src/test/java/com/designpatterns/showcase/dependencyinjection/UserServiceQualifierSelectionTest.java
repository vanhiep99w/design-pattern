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
        "onboarding.fallback-enabled=true",
        "onboarding.welcome-message=Fallback welcomes you, %s!",
        "onboarding.support-email=fallback@test.io"
})
class UserServiceQualifierSelectionTest {

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
    void usesFallbackRepositoryWhenFeatureToggleEnabled() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "john",
                "john@example.com",
                "John",
                "Smith",
                UserRole.USER
        );

        when(inMemoryRepository.existsByUsername("john")).thenReturn(false);
        when(inMemoryRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(inMemoryRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0, User.class);
            user.setId(202L);
            return user;
        });

        User saved = userService.onboardUser(request);

        assertEquals(202L, saved.getId());
        verify(inMemoryRepository).save(any(User.class));
        verify(jpaRepository, never()).save(any(User.class));
        verify(notificationGateway).sendWelcomeNotification(eq(saved), contains("John"));
        verify(auditTrailPublisher).recordUserOnboarding(eq(saved), eq(true));
    }
}
