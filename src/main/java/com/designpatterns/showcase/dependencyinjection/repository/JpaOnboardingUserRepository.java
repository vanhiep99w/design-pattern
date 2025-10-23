package com.designpatterns.showcase.dependencyinjection.repository;

import com.designpatterns.showcase.common.domain.User;
import com.designpatterns.showcase.common.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository("jpaOnboardingUserRepository")
public class JpaOnboardingUserRepository implements OnboardingUserRepository {

    private final UserRepository userRepository;

    public JpaOnboardingUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
