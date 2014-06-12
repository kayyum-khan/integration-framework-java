package com.appearnetworks.aiq.integrationframework.impl.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterAdapterRequest {
    private String integrationURL;
    private String integrationPassword;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public RegisterAdapterRequest() {}

    public RegisterAdapterRequest(String integrationURL, String integrationPassword) {
        this.integrationURL = integrationURL;
        this.integrationPassword = integrationPassword;
    }

    public String getIntegrationURL() {
        return integrationURL;
    }

    public String getIntegrationPassword() {
        return integrationPassword;
    }
}
