package com.designpatterns.showcase.factory;

import java.math.BigDecimal;

public interface PaymentProcessor {
    PaymentResult processPayment(PaymentRequest request);
    boolean validatePaymentDetails(PaymentRequest request);
    String getPaymentType();
}
