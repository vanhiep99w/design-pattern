package com.designpatterns.showcase.observer.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {
    
    private final Long orderId;
    private final Long userId;
    private final BigDecimal totalAmount;
    private final LocalDateTime orderDate;
    
    public OrderCreatedEvent(Object source, Long orderId, Long userId, BigDecimal totalAmount) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.orderDate = LocalDateTime.now();
    }
}
