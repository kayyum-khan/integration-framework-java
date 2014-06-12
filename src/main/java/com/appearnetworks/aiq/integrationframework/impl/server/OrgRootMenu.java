package com.appearnetworks.aiq.integrationframework.impl.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;


@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgRootMenu {

    private ObjectNode links;

    public OrgRootMenu() { }

    public ObjectNode getLinks() {
        return links;
    }
}
