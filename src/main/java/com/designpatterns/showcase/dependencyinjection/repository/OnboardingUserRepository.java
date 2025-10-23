package com.designpatterns.showcase.dependencyinjection.repository;

import com.designpatterns.showcase.common.domain.User;

public interface OnboardingUserRepository {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User save(User user);
}
