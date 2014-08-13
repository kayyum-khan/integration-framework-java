package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the link to get this specific backend message (in the "self" key).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrichedBackendMessageLinks {
    private String self;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public EnrichedBackendMessageLinks() { }

    /**
     * Used for testing.
     */
    EnrichedBackendMessageLinks(String self) {
        this.self = self;
    }

    public String getSelf() {
        return self;
    }
}
