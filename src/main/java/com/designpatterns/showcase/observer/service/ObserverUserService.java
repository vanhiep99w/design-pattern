package com.designpatterns.showcase.observer.service;

import com.designpatterns.showcase.observer.events.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ObserverUserService {

    private final ApplicationEventPublisher eventPublisher;

    public ObserverUserService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Long registerUser(String username, String email) {
        Long userId = System.currentTimeMillis();
        
        log.info("Registering user: UserId={}, Username={}, Email={}", userId, username, email);
        
        UserRegisteredEvent event = new UserRegisteredEvent(this, userId, username, email);
        eventPublisher.publishEvent(event);
        
        log.info("User registered and event published");
        return userId;
    }
}
