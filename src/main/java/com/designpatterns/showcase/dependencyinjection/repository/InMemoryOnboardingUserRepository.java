package com.designpatterns.showcase.dependencyinjection.repository;

import com.designpatterns.showcase.common.domain.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository("inMemoryOnboardingUserRepository")
public class InMemoryOnboardingUserRepository implements OnboardingUserRepository {

    private final AtomicLong sequence = new AtomicLong();
    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();

    @Override
    public boolean existsByUsername(String username) {
        return usersByUsername.containsKey(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return usersByEmail.containsKey(email);
    }

    @Override
    public User save(User user) {
        User persistedUser = cloneForPersistence(user);
        usersByUsername.put(persistedUser.getUsername(), persistedUser);
        usersByEmail.put(persistedUser.getEmail(), persistedUser);
        return persistedUser;
    }

    private User cloneForPersistence(User user) {
        Long id = user.getId() != null ? user.getId() : sequence.incrementAndGet();
        LocalDateTime createdAt = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now();
        LocalDateTime updatedAt = user.getUpdatedAt() != null ? user.getUpdatedAt() : createdAt;

        return User.builder()
                .id(id)
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
