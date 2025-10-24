package com.designpatterns.showcase.strategy.payment;

import com.designpatterns.showcase.strategy.dto.PaymentValidationRequest;
import com.designpatterns.showcase.strategy.dto.PaymentValidationResult;

public interface PaymentValidationStrategy {
    PaymentValidationResult validate(PaymentValidationRequest request);
    String getStrategyName();
}
