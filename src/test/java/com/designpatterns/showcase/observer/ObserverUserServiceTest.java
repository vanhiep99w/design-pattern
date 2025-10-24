package com.designpatterns.showcase.observer;

import com.designpatterns.showcase.observer.events.UserRegisteredEvent;
import com.designpatterns.showcase.observer.listener.UserRegistrationListener;
import com.designpatterns.showcase.observer.service.ObserverUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
class ObserverUserServiceTest {

    @Autowired
    private ObserverUserService userService;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void testUserRegisteredEventIsPublished() {
        String username = "testuser";
        String email = "testuser@example.com";

        Long userId = userService.registerUser(username, email);

        assertThat(userId).isNotNull();
        
        long eventCount = applicationEvents.stream(UserRegisteredEvent.class).count();
        assertThat(eventCount).isEqualTo(1);

        UserRegisteredEvent event = applicationEvents.stream(UserRegisteredEvent.class)
            .findFirst()
            .orElseThrow();

        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getUsername()).isEqualTo(username);
        assertThat(event.getEmail()).isEqualTo(email);
        assertThat(event.getRegistrationDate()).isNotNull();
    }

    @Test
    void testChainedListenersExecute() {
        String username = "chainuser";
        String email = "chainuser@example.com";

        userService.registerUser(username, email);

        await().atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                long userRegisteredCount = applicationEvents.stream(UserRegisteredEvent.class).count();
                assertThat(userRegisteredCount).isGreaterThanOrEqualTo(1);
            });
    }

    @Test
    void testMultipleUserRegistrations() {
        userService.registerUser("user1", "user1@example.com");
        userService.registerUser("user2", "user2@example.com");
        userService.registerUser("user3", "user3@example.com");

        await().atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                long eventCount = applicationEvents.stream(UserRegisteredEvent.class).count();
                assertThat(eventCount).isEqualTo(3);
            });
    }

    @Test
    void testEventChaining() {
        String username = "eventchainuser";
        String email = "eventchain@example.com";

        Long userId = userService.registerUser(username, email);

        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                UserRegisteredEvent userEvent = applicationEvents.stream(UserRegisteredEvent.class)
                    .findFirst()
                    .orElseThrow();
                
                assertThat(userEvent.getUserId()).isEqualTo(userId);
            });
    }
}
