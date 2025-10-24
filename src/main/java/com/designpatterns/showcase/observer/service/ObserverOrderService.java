package com.designpatterns.showcase.observer.service;

import com.designpatterns.showcase.observer.events.OrderCreatedEvent;
import com.designpatterns.showcase.observer.events.OrderDeliveredEvent;
import com.designpatterns.showcase.observer.events.OrderShippedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class ObserverOrderService {

    private final ApplicationEventPublisher eventPublisher;

    public ObserverOrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Long createOrder(Long userId, BigDecimal totalAmount) {
        Long orderId = System.currentTimeMillis();
        
        log.info("Creating order: OrderId={}, UserId={}, Amount={}", orderId, userId, totalAmount);
        
        OrderCreatedEvent event = new OrderCreatedEvent(this, orderId, userId, totalAmount);
        eventPublisher.publishEvent(event);
        
        log.info("Order created and event published");
        return orderId;
    }

    public void shipOrder(Long orderId, Long userId) {
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("Shipping order: OrderId={}, TrackingNumber={}", orderId, trackingNumber);
        
        OrderShippedEvent event = new OrderShippedEvent(this, orderId, userId, trackingNumber);
        eventPublisher.publishEvent(event);
        
        log.info("Order shipped and event published");
    }

    public void deliverOrder(Long orderId, Long userId) {
        log.info("Delivering order: OrderId={}", orderId);
        
        OrderDeliveredEvent event = new OrderDeliveredEvent(this, orderId, userId);
        eventPublisher.publishEvent(event);
        
        log.info("Order delivered and event published");
    }
}
