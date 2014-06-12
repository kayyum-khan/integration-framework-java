package com.appearnetworks.aiq.integrationframework.integration;

/**
 * Thrown by the Integration Adapter to indicate that it is temporary unable to perform the operation,
 * and that the server should retry later.
 */
public class UnavailableException extends Exception {
    private final int retryAfterSeconds;

    /**
     * Unavailable for an unspecified time, the server will use its default retry delay.
     */
    public UnavailableException() {
        this(0);
    }

    /**
     * Unavailable for a specified number of seconds.
     *
     * @param retryAfterSeconds number of seconds to wait before retrying, or 0 to leave it unspecified.
     */
    public UnavailableException(int retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
