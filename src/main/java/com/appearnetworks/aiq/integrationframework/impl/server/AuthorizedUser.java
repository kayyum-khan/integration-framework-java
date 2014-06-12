package com.appearnetworks.aiq.integrationframework.impl.server;

import com.appearnetworks.aiq.integrationframework.server.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the authorized user returned by the server after successful validation of the single sign on token.
 * It contains the server user associated with the single sign on token
 * and is used for the web extension user authentication.
 *
 * @see com.appearnetworks.aiq.integrationframework.server.User
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizedUser {
    private User user;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public AuthorizedUser() { }

    public AuthorizedUser(User user) {
        this.user = user;
    }

    /**
     * The user.
     * @return the authorized user.
     */
    public User getUser() {
        return user;
    }
}
