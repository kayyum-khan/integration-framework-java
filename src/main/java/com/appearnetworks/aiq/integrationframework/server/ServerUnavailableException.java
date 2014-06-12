package com.appearnetworks.aiq.integrationframework.server;

/**
 * Server is temporary unavailable. Retry again slightly later.
 *
 * Corresponds to HTTP status 503 Service Unavailable, or unable to connect to server.
 */
public class ServerUnavailableException extends RuntimeException {

    public ServerUnavailableException() {
    }

    public ServerUnavailableException(String message) {
        super(message);
    }
}
