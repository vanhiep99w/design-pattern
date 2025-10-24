package com.designpatterns.showcase.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PaymentProcessorFactory {

    private final Map<String, PaymentProcessor> processors = new HashMap<>();

    @Autowired
    public PaymentProcessorFactory(List<PaymentProcessor> paymentProcessors) {
        for (PaymentProcessor processor : paymentProcessors) {
            processors.put(processor.getPaymentType(), processor);
            log.info("Registered payment processor: {}", processor.getPaymentType());
        }
    }

    public PaymentProcessor getPaymentProcessor(String paymentType) {
        if (paymentType == null) {
            throw new IllegalArgumentException("Payment type cannot be null");
        }

        String normalizedType = paymentType.toUpperCase().trim();
        PaymentProcessor processor = processors.get(normalizedType);

        if (processor == null) {
            throw new UnsupportedPaymentTypeException("Unsupported payment type: " + paymentType);
        }

        log.debug("Retrieved payment processor for type: {}", normalizedType);
        return processor;
    }

    public boolean isPaymentTypeSupported(String paymentType) {
        if (paymentType == null) {
            return false;
        }
        return processors.containsKey(paymentType.toUpperCase().trim());
    }

    public Map<String, PaymentProcessor> getAllProcessors() {
        return new HashMap<>(processors);
    }
}
