package com.designpatterns.showcase.observer;

import com.designpatterns.showcase.observer.events.OrderCreatedEvent;
import com.designpatterns.showcase.observer.events.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RecordApplicationEvents
class ObserverDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void testCreateOrderEndpoint() throws Exception {
        mockMvc.perform(post("/api/observer/orders")
                .param("userId", "1")
                .param("amount", "99.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.message").exists());

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    long eventCount = applicationEvents.stream(OrderCreatedEvent.class).count();
                    assertThat(eventCount).isGreaterThanOrEqualTo(1);
                });
    }

    @Test
    void testShipOrderEndpoint() throws Exception {
        mockMvc.perform(put("/api/observer/orders/123/ship")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("123"))
                .andExpect(jsonPath("$.status").value("shipped"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testDeliverOrderEndpoint() throws Exception {
        mockMvc.perform(put("/api/observer/orders/123/deliver")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("123"))
                .andExpect(jsonPath("$.status").value("delivered"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRegisterUserEndpoint() throws Exception {
        mockMvc.perform(post("/api/observer/users/register")
                .param("username", "testuser")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").exists());

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    long eventCount = applicationEvents.stream(UserRegisteredEvent.class).count();
                    assertThat(eventCount).isGreaterThanOrEqualTo(1);
                });
    }

    @Test
    void testGetInfoEndpoint() throws Exception {
        mockMvc.perform(get("/api/observer/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Observer"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.features").exists());
    }
}
