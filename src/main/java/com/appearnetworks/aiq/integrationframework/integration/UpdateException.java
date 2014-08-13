package com.appearnetworks.aiq.integrationframework.integration;

import org.springframework.http.HttpStatus;

/**
 * Update failed with HTTP status code.
 *<p/>
 * These status codes are used:
 * <ul>
 * <li>{@link org.springframework.http.HttpStatus#FORBIDDEN} - violation of business rule or permission denied</li>
 * <li>{@link org.springframework.http.HttpStatus#NOT_FOUND} - unrecognized document type</li>
 * <li>{@link org.springframework.http.HttpStatus#METHOD_NOT_ALLOWED} - documents of this type cannot be created/updated/deleted from clients</li>
 * <li>{@link org.springframework.http.HttpStatus#CONFLICT} - duplicate document id or attachment name on create</li>
 * <li>{@link org.springframework.http.HttpStatus#PRECONDITION_FAILED} - optimistic locking failed on update or delete</li>
 * <li>{@link org.springframework.http.HttpStatus#REQUEST_ENTITY_TOO_LARGE} - attachment too large</li>
 * </ul>
 */
public class UpdateException extends Exception {
    private final HttpStatus statusCode;

    /**
     * @param statusCode HTTP status code
     */
    public UpdateException(HttpStatus statusCode) {
        super(statusCode.toString());
        this.statusCode = statusCode;
    }

    /**
     * @return HTTP status code
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
