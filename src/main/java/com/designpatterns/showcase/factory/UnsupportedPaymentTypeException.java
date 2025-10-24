package com.designpatterns.showcase.factory;

public class UnsupportedPaymentTypeException extends RuntimeException {
    public UnsupportedPaymentTypeException(String message) {
        super(message);
    }
}
