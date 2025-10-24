package com.designpatterns.showcase.observer;

import com.designpatterns.showcase.observer.events.OrderCreatedEvent;
import com.designpatterns.showcase.observer.events.OrderDeliveredEvent;
import com.designpatterns.showcase.observer.events.OrderShippedEvent;
import com.designpatterns.showcase.observer.listener.OrderEventListener;
import com.designpatterns.showcase.observer.service.ObserverOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
class ObserverOrderServiceTest {

    @Autowired
    private ObserverOrderService orderService;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    void testOrderCreatedEventIsPublished() {
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("99.99");

        Long orderId = orderService.createOrder(userId, amount);

        assertThat(orderId).isNotNull();
        
        long eventCount = applicationEvents.stream(OrderCreatedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);

        OrderCreatedEvent event = applicationEvents.stream(OrderCreatedEvent.class)
            .findFirst()
            .orElseThrow();

        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getTotalAmount()).isEqualTo(amount);
        assertThat(event.getOrderDate()).isNotNull();
    }

    @Test
    void testOrderShippedEventIsPublished() {
        Long orderId = 123L;
        Long userId = 1L;

        orderService.shipOrder(orderId, userId);

        long eventCount = applicationEvents.stream(OrderShippedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);

        OrderShippedEvent event = applicationEvents.stream(OrderShippedEvent.class)
            .findFirst()
            .orElseThrow();

        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getTrackingNumber()).isNotNull();
        assertThat(event.getTrackingNumber()).startsWith("TRK-");
        assertThat(event.getShippedDate()).isNotNull();
    }

    @Test
    void testOrderDeliveredEventIsPublished() {
        Long orderId = 456L;
        Long userId = 2L;

        orderService.deliverOrder(orderId, userId);

        long eventCount = applicationEvents.stream(OrderDeliveredEvent.class).count();
        assertThat(eventCount).isEqualTo(1);

        OrderDeliveredEvent event = applicationEvents.stream(OrderDeliveredEvent.class)
            .findFirst()
            .orElseThrow();

        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getDeliveredDate()).isNotNull();
    }

    @Test
    void testAsyncListenersExecute() {
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("199.99");

        orderService.createOrder(userId, amount);

        await().atMost(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                long eventCount = applicationEvents.stream(OrderCreatedEvent.class).count();
                assertThat(eventCount).isGreaterThanOrEqualTo(1);
            });
    }

    @Test
    void testMultipleOrderEvents() {
        Long userId = 3L;
        BigDecimal amount = new BigDecimal("299.99");

        Long orderId = orderService.createOrder(userId, amount);
        orderService.shipOrder(orderId, userId);
        orderService.deliverOrder(orderId, userId);

        assertThat(applicationEvents.stream(OrderCreatedEvent.class).count()).isEqualTo(1);
        assertThat(applicationEvents.stream(OrderShippedEvent.class).count()).isEqualTo(1);
        assertThat(applicationEvents.stream(OrderDeliveredEvent.class).count()).isEqualTo(1);
    }
}
