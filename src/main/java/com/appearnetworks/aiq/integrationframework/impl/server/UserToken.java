package com.appearnetworks.aiq.integrationframework.impl.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the single sign on token to be validated by the server. This token is used in web extension authorization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserToken {
    private String token;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public UserToken() { }

    public UserToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
