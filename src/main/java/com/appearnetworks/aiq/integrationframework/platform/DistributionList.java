package com.appearnetworks.aiq.integrationframework.platform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;

/**
 * Distribution list for backend messages.
 *
 * @see com.appearnetworks.aiq.integrationframework.platform.BackendMessageRecipients
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistributionList {
    private Collection<String> users;
    private String _id;
    private long _rev;
    private DistributionListLinks links;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public DistributionList() {}

    /**
     * Needed by framework, do not use.
     */
    public DistributionList(Collection<String> users, String _id) {
        this.users = users;
        this._id = _id;
    }

    public Collection<String> getUsers() {
        return users;
    }

    public String get_id() {
        return _id;
    }

    public long get_rev() {
        return _rev;
    }

    public DistributionListLinks getLinks() {
        return links;
    }
}
