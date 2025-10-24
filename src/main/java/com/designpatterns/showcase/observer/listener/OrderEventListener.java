package com.designpatterns.showcase.observer.listener;

import com.designpatterns.showcase.observer.events.OrderCreatedEvent;
import com.designpatterns.showcase.observer.events.OrderDeliveredEvent;
import com.designpatterns.showcase.observer.events.OrderShippedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @EventListener
    @Order(1)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order created: OrderId={}, UserId={}, Amount={}", 
            event.getOrderId(), event.getUserId(), event.getTotalAmount());
        log.info("Thread: {}", Thread.currentThread().getName());
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void sendOrderConfirmationEmail(OrderCreatedEvent event) {
        log.info("Sending order confirmation email for OrderId={}", event.getOrderId());
        log.info("Async Thread: {}", Thread.currentThread().getName());
        simulateEmailDelivery();
        log.info("Order confirmation email sent successfully for OrderId={}", event.getOrderId());
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void notifyWarehouse(OrderCreatedEvent event) {
        log.info("Notifying warehouse for OrderId={}", event.getOrderId());
        log.info("Async Thread: {}", Thread.currentThread().getName());
        simulateWarehouseNotification();
        log.info("Warehouse notified successfully for OrderId={}", event.getOrderId());
    }

    @EventListener
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("Order shipped: OrderId={}, TrackingNumber={}", 
            event.getOrderId(), event.getTrackingNumber());
        log.info("Thread: {}", Thread.currentThread().getName());
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void sendShippingNotification(OrderShippedEvent event) {
        log.info("Sending shipping notification for OrderId={}, Tracking={}", 
            event.getOrderId(), event.getTrackingNumber());
        log.info("Async Thread: {}", Thread.currentThread().getName());
        simulateEmailDelivery();
        log.info("Shipping notification sent successfully");
    }

    @EventListener
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("Order delivered: OrderId={}, UserId={}", 
            event.getOrderId(), event.getUserId());
        log.info("Thread: {}", Thread.currentThread().getName());
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void requestFeedback(OrderDeliveredEvent event) {
        log.info("Requesting customer feedback for OrderId={}", event.getOrderId());
        log.info("Async Thread: {}", Thread.currentThread().getName());
        simulateEmailDelivery();
        log.info("Feedback request sent successfully");
    }

    private void simulateEmailDelivery() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email delivery interrupted", e);
        }
    }

    private void simulateWarehouseNotification() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Warehouse notification interrupted", e);
        }
    }
}
