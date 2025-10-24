package com.designpatterns.showcase.factory;

public class UnsupportedNotificationTypeException extends RuntimeException {
    public UnsupportedNotificationTypeException(String message) {
        super(message);
    }
}
