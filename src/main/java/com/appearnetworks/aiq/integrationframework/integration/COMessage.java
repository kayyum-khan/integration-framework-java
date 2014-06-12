package com.appearnetworks.aiq.integrationframework.integration;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Date;

/**
 * Represents a Client Originated message.
 */
public class COMessage {
    private final String messageId;
    private final String userId;
    private final String deviceId;
    private final long created;
    private final ObjectNode payload;
    private final ObjectNode context;

    public COMessage(String messageId, String userId, String deviceId, long created, ObjectNode payload, ObjectNode context) {
        this.messageId = messageId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.created = created;
        this.payload = payload;
        this.context = context;
    }

    /**
     * @return  the unique ID for this message
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * @return  id of the user who created this message
     *
     * @see com.appearnetworks.aiq.integrationframework.server.IntegrationService#fetchUser(String)
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return  id of the device on which this message was created
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @return  when the message was created on the device
     */
    public Date getCreated() {
        return new Date(created);
    }

    /**
     * @return the message payload
     */
    public ObjectNode getPayload() {
        return payload;
    }

    /**
     * @return the context of message creation
     */
    public ObjectNode getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "COMessage{" +
                "messageId='" + messageId + '\'' +
                ", userId='" + userId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", created=" + created +
                ", payload=" + payload +
                ", context=" + context +
                '}';
    }
}
