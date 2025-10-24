package com.designpatterns.showcase.strategy.exception;

public class UnsupportedStrategyException extends RuntimeException {
    public UnsupportedStrategyException(String message) {
        super(message);
    }
}
