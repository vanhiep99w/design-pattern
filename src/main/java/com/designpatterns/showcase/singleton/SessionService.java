package com.designpatterns.showcase.singleton;

import java.time.LocalDateTime;
import java.util.UUID;

public class SessionService {

    private final String sessionId;
    private final LocalDateTime createdAt;
    private int requestCount;

    public SessionService() {
        this.sessionId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.requestCount = 0;
    }

    public String getSessionId() {
        return sessionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void incrementRequestCount() {
        requestCount++;
    }

    public String processRequest(String requestData) {
        incrementRequestCount();
        return String.format("Session %s processed request #%d: %s",
                sessionId.substring(0, 8), requestCount, requestData);
    }
}
