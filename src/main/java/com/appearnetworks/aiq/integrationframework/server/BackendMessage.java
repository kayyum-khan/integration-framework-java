package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * Represents the backend message.
 * Contains backend message attributes application defined message payload (any valid JSON value) and message notification.
 *
 * @see BackendMessageRecipients
 * @see BackendMessageNotification
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BackendMessage {
    private String type;
    private Long activeFrom;
    private int timeToLive;
    private boolean urgent;
    private String _launchable;
    private ObjectNode payload;
    private BackendMessageRecipients recipients;
    private BackendMessageNotification notification;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public BackendMessage() {}

    public BackendMessage(String type, Date activeFrom, int timeToLive, boolean urgent, String _launchable,
                          ObjectNode payload, BackendMessageNotification notification) {
        this(type, activeFrom, timeToLive, urgent, _launchable, payload, null, notification);
    }

    /**
     * Main constructor.
     *
     * @param type the type of backend message, cannot be {@code null} or empty.
     * @param activeFrom the time from which backend message becomes active, use {@code null} to have the message active immediately.
     * @param timeToLive the duration, in seconds, for which backend message remains active, counted from activeFrom.
     * @param urgent flag true or false if the message is urgent or non-urgent, if true push notifications will be sent to the users.
     * @param _launchable the id of launchable this message belongs to, if {@code null} the message is not assigned to any particular launchable.
     * @param payload application defined payload of the message, cannot be {@code null}, cannot be larger than 1 MiB
     * @param recipients message recipients, or {@code null} to send message to everyone
     * @param notification the notification to display to user upon receiving the message, can be {@code null}
     */
    public BackendMessage(String type, Date activeFrom, int timeToLive, boolean urgent, String _launchable,
                          ObjectNode payload, BackendMessageRecipients recipients,
                          BackendMessageNotification notification) {
        Assert.hasLength(type, "Type cannot be null or empty");
        Assert.isTrue(timeToLive >= 0, "Time to live cannot be negative");
        Assert.notNull(payload, "Payload cannot be null");
        this.type = type;
        this.activeFrom = (activeFrom != null) ? activeFrom.getTime() : null;
        this.timeToLive = timeToLive;
        this.urgent = urgent;
        this.payload = payload;
        this.recipients = recipients;
        this.notification = notification;
        this._launchable = _launchable;
    }

    public String getType() {
        return type;
    }

    public Date getActiveFrom() {
        return (activeFrom != null) ? new Date(activeFrom) : null;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public ObjectNode getPayload() {
        return payload;
    }

    public String get_launchable() {
        return _launchable;
    }

    public BackendMessageRecipients getRecipients() {
        return recipients;
    }

    public BackendMessageNotification getNotification() {
        return notification;
    }
}
