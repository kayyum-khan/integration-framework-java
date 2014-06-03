package com.appearnetworks.aiq.integrationframework.platform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the link to get this specific distribution list (in the "self" key).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistributionListLinks {
    private String self;

    public DistributionListLinks() { }

    public String getSelf() {
        return self;
    }
}
