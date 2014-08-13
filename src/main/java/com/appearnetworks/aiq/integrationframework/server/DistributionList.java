package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;

/**
 * Distribution list for backend messages.
 *
 * @see com.appearnetworks.aiq.integrationframework.server.BackendMessageRecipients
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

    /**
     * Distribution lists can only be created on the server and this constructor should only be used for unit testing.
     */
    public DistributionList(Collection<String> users, String _id, long _rev, DistributionListLinks links) {
        this.users = users;
        this._id = _id;
        this._rev = _rev;
        this.links = links;
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
