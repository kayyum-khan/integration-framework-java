package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the link to get this specific session (in the "self" key).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientSessionLinks {
    private String self;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public ClientSessionLinks() { }

    /**
     * Used for testing.
     */
    ClientSessionLinks(String self) {
        this.self = self;
    }

    public String getSelf() {
        return self;
    }
}
