package com.designpatterns.showcase.observer.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    
    private final Long userId;
    private final String username;
    private final String email;
    private final LocalDateTime registrationDate;
    
    public UserRegisteredEvent(Object source, Long userId, String username, String email) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.registrationDate = LocalDateTime.now();
    }
}
