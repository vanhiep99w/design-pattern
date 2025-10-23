package com.designpatterns.showcase.dependencyinjection.dto;

import com.designpatterns.showcase.common.domain.User;
import com.designpatterns.showcase.common.domain.UserRole;

import java.util.Objects;

public record UserRegistrationRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        UserRole role
) {

    public User toDomainUser() {
        UserRole resolvedRole = Objects.requireNonNullElse(role, UserRole.USER);
        return User.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(resolvedRole)
                .active(true)
                .build();
    }
}
