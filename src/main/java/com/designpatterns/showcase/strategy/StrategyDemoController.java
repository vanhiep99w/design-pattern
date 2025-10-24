package com.designpatterns.showcase.strategy;

import com.designpatterns.showcase.strategy.context.DiscountContext;
import com.designpatterns.showcase.strategy.context.PaymentValidationContext;
import com.designpatterns.showcase.strategy.context.PricingService;
import com.designpatterns.showcase.strategy.context.ShippingContext;
import com.designpatterns.showcase.strategy.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/strategy")
@RequiredArgsConstructor
public class StrategyDemoController {

    private final DiscountContext discountContext;
    private final ShippingContext shippingContext;
    private final PaymentValidationContext paymentValidationContext;
    private final PricingService pricingService;

    @PostMapping("/discount/{strategy}")
    public ResponseEntity<DiscountResult> applyDiscount(
            @PathVariable String strategy,
            @RequestBody DiscountRequest request) {
        log.info("Apply discount request received for strategy: {}", strategy);
        DiscountResult result = discountContext.applyDiscount(strategy, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/discount/best")
    public ResponseEntity<DiscountResult> applyBestDiscount(@RequestBody DiscountRequest request) {
        log.info("Apply best discount request received");
        DiscountResult result = discountContext.applyBestDiscount(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/shipping/{strategy}")
    public ResponseEntity<ShippingCostResult> calculateShipping(
            @PathVariable String strategy,
            @RequestBody ShippingRequest request) {
        log.info("Calculate shipping request received for strategy: {}", strategy);
        ShippingCostResult result = shippingContext.calculateShippingCost(strategy, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/shipping/auto")
    public ResponseEntity<ShippingCostResult> calculateShippingAuto(@RequestBody ShippingRequest request) {
        log.info("Auto calculate shipping request received");
        ShippingCostResult result = shippingContext.calculateShippingCostAuto(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payment/validate/{strategy}")
    public ResponseEntity<PaymentValidationResult> validatePayment(
            @PathVariable String strategy,
            @RequestBody PaymentValidationRequest request) {
        log.info("Payment validation request received for strategy: {}", strategy);
        PaymentValidationResult result = paymentValidationContext.validatePayment(strategy, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payment/validate/all")
    public ResponseEntity<PaymentValidationResult> validatePaymentAll(
            @RequestBody PaymentValidationRequest request) {
        log.info("Payment validation with all strategies request received");
        PaymentValidationResult result = paymentValidationContext.validatePaymentWithAllStrategies(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/pricing")
    public ResponseEntity<PricingResponse> calculateTotalPrice(@RequestBody PricingRequest request) {
        log.info("Total pricing calculation request received");
        PricingResponse response = pricingService.calculateTotalPrice(request);
        return ResponseEntity.ok(response);
    }
}
