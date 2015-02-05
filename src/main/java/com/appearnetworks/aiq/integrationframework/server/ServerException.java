package com.appearnetworks.aiq.integrationframework.server;

import org.springframework.http.HttpStatus;

import java.net.URI;

/**
 * Unexpected error response received from server.
 */
public class ServerException extends RuntimeException {
    private final URI url;
    private final HttpStatus statusCode;
    private final String errorMessage;

    public ServerException(URI url, HttpStatus statusCode, String errorMessage) {
        super("HTTP response with status code [" + statusCode + "] and error message [" + errorMessage + "] for URL [" + url + "]");
        this.url = url;
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public ServerException(URI url, String message) {
        super(message + " for URL [" + url + "]");
        this.url = url;
        this.statusCode = null;
        this.errorMessage = null;
    }

    /**
     * @return the URL causing the error
     */
    public URI getUrl() {
        return url;
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
