package com.appearnetworks.aiq.integrationframework.platform;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.Assert;

/**
 * Represents the notification to be display to user upon receiving a backend message
 *
 * @see BackendMessage
 */
public class BackendMessageNotification {
    private boolean sound;
    private boolean vibration;
    private String message;

    private ObjectNode condition;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public BackendMessageNotification() { }

    /**
     * Main constructor.
     *
     * @param sound if set to true sound will be played upon receiving of the message.
     * @param vibration if set to true the devices will vibrate upon receiving of the message.
     * @param message the message to display upon receiving the message, cannot be {@code null}.
     * @param condition evaluated to check for which users message is relevant for,
     *                  only devices matching this condition will receive a push notification,
     *                  but all recipients will eventually receive the message,
     *                  use {@code null} for all recipients
     */
    public BackendMessageNotification(boolean sound, boolean vibration, String message, ObjectNode condition) {
        Assert.notNull(message, "Notification message cannot be null");
        this.sound = sound;
        this.vibration = vibration;
        this.message = message;
        this.condition = condition;
    }

    public boolean isSound() {
        return sound;
    }

    public boolean isVibration() {
        return vibration;
    }

    public String getMessage() {
        return message;
    }

    public ObjectNode getCondition() {
        return condition;
    }
}
