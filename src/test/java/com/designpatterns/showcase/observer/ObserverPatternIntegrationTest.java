package com.designpatterns.showcase.observer;

import com.designpatterns.showcase.observer.events.OrderCreatedEvent;
import com.designpatterns.showcase.observer.events.OrderDeliveredEvent;
import com.designpatterns.showcase.observer.events.OrderShippedEvent;
import com.designpatterns.showcase.observer.events.UserRegisteredEvent;
import com.designpatterns.showcase.observer.service.ObserverOrderService;
import com.designpatterns.showcase.observer.service.ObserverUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class ObserverPatternIntegrationTest {

    @Autowired
    private ObserverOrderService orderService;

    @Autowired
    private ObserverUserService userService;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void testCompleteOrderLifecycle() {
        Long userId = 100L;
        BigDecimal amount = new BigDecimal("299.99");

        Long orderId = orderService.createOrder(userId, amount);
        orderService.shipOrder(orderId, userId);
        orderService.deliverOrder(orderId, userId);

        await().atMost(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(applicationEvents.stream(OrderCreatedEvent.class).count()).isEqualTo(1);
                assertThat(applicationEvents.stream(OrderShippedEvent.class).count()).isEqualTo(1);
                assertThat(applicationEvents.stream(OrderDeliveredEvent.class).count()).isEqualTo(1);
            });

        OrderCreatedEvent createdEvent = applicationEvents.stream(OrderCreatedEvent.class)
            .findFirst()
            .orElseThrow();
        assertThat(createdEvent.getOrderId()).isEqualTo(orderId);
        assertThat(createdEvent.getUserId()).isEqualTo(userId);
        assertThat(createdEvent.getTotalAmount()).isEqualTo(amount);

        OrderShippedEvent shippedEvent = applicationEvents.stream(OrderShippedEvent.class)
            .findFirst()
            .orElseThrow();
        assertThat(shippedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(shippedEvent.getTrackingNumber()).isNotNull();

        OrderDeliveredEvent deliveredEvent = applicationEvents.stream(OrderDeliveredEvent.class)
            .findFirst()
            .orElseThrow();
        assertThat(deliveredEvent.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void testUserRegistrationWithAsyncListeners() {
        String username = "asyncuser";
        String email = "async@example.com";

        Long userId = userService.registerUser(username, email);

        assertThat(userId).isNotNull();

        await().atMost(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                long eventCount = applicationEvents.stream(UserRegisteredEvent.class).count();
                assertThat(eventCount).isGreaterThanOrEqualTo(1);
            });

        UserRegisteredEvent event = applicationEvents.stream(UserRegisteredEvent.class)
            .filter(e -> e.getUsername().equals(username))
            .findFirst()
            .orElseThrow();

        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getEmail()).isEqualTo(email);
    }

    @Test
    void testMultipleEventsInParallel() {
        userService.registerUser("parallel1", "p1@example.com");
        userService.registerUser("parallel2", "p2@example.com");
        
        orderService.createOrder(200L, new BigDecimal("99.99"));
        orderService.createOrder(201L, new BigDecimal("199.99"));

        await().atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                long userEvents = applicationEvents.stream(UserRegisteredEvent.class).count();
                long orderEvents = applicationEvents.stream(OrderCreatedEvent.class).count();
                
                assertThat(userEvents).isGreaterThanOrEqualTo(2);
                assertThat(orderEvents).isGreaterThanOrEqualTo(2);
            });
    }
}
