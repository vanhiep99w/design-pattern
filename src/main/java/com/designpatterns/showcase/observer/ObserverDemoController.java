package com.designpatterns.showcase.observer;

import com.designpatterns.showcase.observer.service.ObserverOrderService;
import com.designpatterns.showcase.observer.service.ObserverUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/observer")
@Slf4j
public class ObserverDemoController {

    private final ObserverOrderService orderService;
    private final ObserverUserService userService;

    public ObserverDemoController(ObserverOrderService orderService, ObserverUserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        
        log.info("REST API: Creating order for userId={}, amount={}", userId, amount);
        
        Long orderId = orderService.createOrder(userId, amount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("userId", userId);
        response.put("amount", amount);
        response.put("message", "Order created successfully. Check logs for async event processing.");
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/orders/{orderId}/ship")
    public ResponseEntity<Map<String, String>> shipOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        
        log.info("REST API: Shipping order orderId={}, userId={}", orderId, userId);
        
        orderService.shipOrder(orderId, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("orderId", orderId.toString());
        response.put("status", "shipped");
        response.put("message", "Order shipped. Shipping notification will be sent asynchronously.");
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/orders/{orderId}/deliver")
    public ResponseEntity<Map<String, String>> deliverOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        
        log.info("REST API: Delivering order orderId={}, userId={}", orderId, userId);
        
        orderService.deliverOrder(orderId, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("orderId", orderId.toString());
        response.put("status", "delivered");
        response.put("message", "Order delivered. Feedback request will be sent asynchronously.");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @RequestParam String username,
            @RequestParam String email) {
        
        log.info("REST API: Registering user username={}, email={}", username, email);
        
        Long userId = userService.registerUser(username, email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("username", username);
        response.put("email", email);
        response.put("message", "User registered successfully. Check logs for async event chaining (welcome email → profile creation → external notification).");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("pattern", "Observer");
        info.put("description", "Demonstrates Spring event-driven architecture with synchronous and asynchronous listeners");
        info.put("features", "ApplicationEventPublisher, @EventListener, @Async, Event Chaining");
        
        return ResponseEntity.ok(info);
    }
}
