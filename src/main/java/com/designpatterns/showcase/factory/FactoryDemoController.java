package com.designpatterns.showcase.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/factory-demo")
@RequiredArgsConstructor
public class FactoryDemoController {

    private final OrderProcessingService orderProcessingService;

    @PostMapping("/process-order")
    public ResponseEntity<OrderProcessingResult> processOrder(@RequestBody OrderProcessingRequest request) {
        log.info("Received order processing request for customer: {}", request.getCustomerId());
        
        try {
            OrderProcessingResult result = orderProcessingService.processOrder(request);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (UnsupportedPaymentTypeException e) {
            log.error("Unsupported payment type: {}", e.getMessage());
            OrderProcessingResult errorResult = OrderProcessingResult.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        } catch (Exception e) {
            log.error("Error processing order", e);
            OrderProcessingResult errorResult = OrderProcessingResult.builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    @GetMapping("/supported-payment-types")
    public ResponseEntity<Map<String, Boolean>> getSupportedPaymentTypes() {
        log.info("Fetching supported payment types");
        return ResponseEntity.ok(orderProcessingService.getSupportedPaymentTypes());
    }

    @GetMapping("/supported-notification-types")
    public ResponseEntity<Map<String, Boolean>> getSupportedNotificationTypes() {
        log.info("Fetching supported notification types");
        return ResponseEntity.ok(orderProcessingService.getSupportedNotificationTypes());
    }
}
