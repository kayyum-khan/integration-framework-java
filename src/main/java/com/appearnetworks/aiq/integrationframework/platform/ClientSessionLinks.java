package com.appearnetworks.aiq.integrationframework.platform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the link to get this specific session (in the "self" key).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientSessionLinks {
    private String self;

    public ClientSessionLinks() { }

    public String getSelf() {
        return self;
    }
}
