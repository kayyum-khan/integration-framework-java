package com.appearnetworks.aiq.integrationframework.impl.integration;

public final class LogoutRequest {
    public String userId;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public LogoutRequest() { }

    public LogoutRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
