package com.appearnetworks.aiq.integrationframework.platform;

import java.util.Collection;

/**
 * Represents the recipients of a backend message.
 *
 * If both users and distributionLists are {@code null}, the message will be sent to everyone.
 * If both are set, the message will be sent to all users included in any of them.
 *
 * @see com.appearnetworks.aiq.integrationframework.platform.BackendMessage
 */
public class BackendMessageRecipients {
    private Collection<String> users;
    private Collection<String> distributionLists;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public BackendMessageRecipients() { }

    /**
     * Main constructor.
     *
     * @param users IDs of users who should receive the message,
     *              can be {@code null} to not restrict to particular users.
     * @param distributionLists IDs of distribution lists who should receive the message,
     *              can be {@code null} to not restrict to distribution lists.
     */
    public BackendMessageRecipients(Collection<String> users, Collection<String> distributionLists) {
        this.users = users;
        this.distributionLists = distributionLists;
    }

    public Collection<String> getUsers() {
        return users;
    }

    public Collection<String> getDistributionLists() {
        return distributionLists;
    }
}
