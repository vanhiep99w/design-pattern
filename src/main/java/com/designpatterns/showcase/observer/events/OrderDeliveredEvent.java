package com.designpatterns.showcase.observer.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class OrderDeliveredEvent extends ApplicationEvent {
    
    private final Long orderId;
    private final Long userId;
    private final LocalDateTime deliveredDate;
    
    public OrderDeliveredEvent(Object source, Long orderId, Long userId) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.deliveredDate = LocalDateTime.now();
    }
}
