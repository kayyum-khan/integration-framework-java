package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the link to get this specific distribution list (in the "self" key).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistributionListLinks {
    private String self;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public DistributionListLinks() { }

    /**
     * Used for testing.
     */
    DistributionListLinks(String self) {
        this.self = self;
    }

    public String getSelf() {
        return self;
    }
}
