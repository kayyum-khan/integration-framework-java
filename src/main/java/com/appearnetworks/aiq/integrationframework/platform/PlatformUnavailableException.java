package com.appearnetworks.aiq.integrationframework.platform;

/**
 * Platform is temporary unavailable. Retry again slightly later.
 *
 * Corresponds to HTTP status 503 Service Unavailable, or unable to connect to platform.
 */
public class PlatformUnavailableException extends RuntimeException {

    public PlatformUnavailableException() {
    }

    public PlatformUnavailableException(String message) {
        super(message);
    }
}
