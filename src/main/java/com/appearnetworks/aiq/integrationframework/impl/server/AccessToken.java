package com.appearnetworks.aiq.integrationframework.impl.server;

import com.appearnetworks.aiq.integrationframework.server.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents the access token returned by server after successful authentication of integration adapter user.
 * This token is kept in cache and is later used for authorization purpose.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessToken {
    private String access_token;
    private String token_type;
    private String scope;
    private String expires_in;
    private User user;
    private ObjectNode links;

    public AccessToken() { }

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public String getScope() {
        return scope;
    }

    public User getUser() {
        return user;
    }

    public ObjectNode getLinks() {
        return links;
    }
}
