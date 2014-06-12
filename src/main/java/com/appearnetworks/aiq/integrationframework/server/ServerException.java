package com.appearnetworks.aiq.integrationframework.server;

import org.springframework.http.HttpStatus;

/**
 * Unexpected error response received from server.
 */
public class ServerException extends RuntimeException {
    private final HttpStatus statusCode;
    private final String errorMessage;

    public ServerException(HttpStatus statusCode, String errorMessage) {
        super("HTTP response with status code [" + statusCode + "] and error message [" + errorMessage + "]");
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public ServerException(String message) {
        super(message);
        this.statusCode = null;
        this.errorMessage = null;
    }

    /**
     * @return HTTP status code, or {@code null} if there were no proper HTTP response
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    /**
     * @return error message in HTTP response, or {@code null} if there were no proper HTTP response
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
