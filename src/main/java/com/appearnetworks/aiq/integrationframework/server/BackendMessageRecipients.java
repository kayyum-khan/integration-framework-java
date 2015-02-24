package com.appearnetworks.aiq.integrationframework.server;

import java.util.Collection;

/**
 * Represents the recipients of a backend message.
 *
 * If users are {@code null}, the message will be sent to everyone.
 *
 * @see com.appearnetworks.aiq.integrationframework.server.BackendMessage
 */
public class BackendMessageRecipients {
    private Collection<String> users;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public BackendMessageRecipients() { }

    /**
     * Main constructor.
     *
     * @param users IDs of users who should receive the message,
     *              can be {@code null} to not restrict to particular users.
     */
    public BackendMessageRecipients(Collection<String> users) {
        this.users = users;
    }

    public Collection<String> getUsers() {
        return users;
    }
}
