package com.appearnetworks.aiq.integrationframework.impl.platform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the single sign on token to be validated by the platform. This token is used in web extension authorization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformUserToken {
    private String token;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public PlatformUserToken() { }

    public PlatformUserToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
