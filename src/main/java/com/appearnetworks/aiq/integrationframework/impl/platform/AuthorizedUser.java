package com.appearnetworks.aiq.integrationframework.impl.platform;

import com.appearnetworks.aiq.integrationframework.platform.PlatformUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the authorized user returned by the platform after successful validation of the single sign on token.
 * It contains the platform user associated with the single sign on token
 * and is used for the web extension user authentication.
 *
 * @see com.appearnetworks.aiq.integrationframework.platform.PlatformUser
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizedUser {
    private PlatformUser user;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public AuthorizedUser() { }

    public AuthorizedUser(PlatformUser user) {
        this.user = user;
    }

    /**
     * The platform user.
     * @return the authorized platform user.
     */
    public PlatformUser getUser() {
        return user;
    }
}
