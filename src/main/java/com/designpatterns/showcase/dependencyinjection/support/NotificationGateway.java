package com.designpatterns.showcase.dependencyinjection.support;

import com.designpatterns.showcase.common.domain.User;

public interface NotificationGateway {

    void sendWelcomeNotification(User user, String message);
}
