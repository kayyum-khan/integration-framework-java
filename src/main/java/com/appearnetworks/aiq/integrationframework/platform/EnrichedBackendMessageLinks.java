package com.appearnetworks.aiq.integrationframework.platform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the link to get this specific backend message (in the "self" key).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrichedBackendMessageLinks {
    private String self;

    public EnrichedBackendMessageLinks() { }

    public String getSelf() {
        return self;
    }
}
