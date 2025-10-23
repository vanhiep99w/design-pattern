package com.designpatterns.showcase.dependencyinjection.support;

import com.designpatterns.showcase.common.domain.User;

public interface AuditTrailPublisher {

    void recordUserOnboarding(User user, boolean fallbackRepositoryUsed);
}
