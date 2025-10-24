package com.designpatterns.showcase.observer.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class OrderShippedEvent extends ApplicationEvent {
    
    private final Long orderId;
    private final Long userId;
    private final String trackingNumber;
    private final LocalDateTime shippedDate;
    
    public OrderShippedEvent(Object source, Long orderId, Long userId, String trackingNumber) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.trackingNumber = trackingNumber;
        this.shippedDate = LocalDateTime.now();
    }
}
