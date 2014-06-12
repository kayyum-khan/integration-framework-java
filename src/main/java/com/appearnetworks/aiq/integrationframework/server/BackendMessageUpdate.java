package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents the updates that should be made on a backend message. Contains the fields that can be updated for a backend message.
 *
 * @see BackendMessage
 */
public class BackendMessageUpdate {
    private int timeToLive;
    private ObjectNode payload;
    private BackendMessageNotification notification;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public BackendMessageUpdate() { }

    /**
     * Main constructor.
     *
     * @param timeToLive the duration for which backend message remains active counted from activeFrom. If time to live is not required to updated it can be set to 0
     * @param payload application defined payload of the backend message (any valid JSON value). If payload is not required to be updated it can be set to {@code null}
     * @param notification the notification to display to user upon receiving the message. If notification is not required to be updated it can be set to {@code null}
     */
    public BackendMessageUpdate(int timeToLive, ObjectNode payload, BackendMessageNotification notification) {
        this.timeToLive = timeToLive;
        this.payload = payload;
        this.notification = notification;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public ObjectNode getPayload() {
        return payload;
    }

    public BackendMessageNotification getNotification() {
        return notification;
    }
}
