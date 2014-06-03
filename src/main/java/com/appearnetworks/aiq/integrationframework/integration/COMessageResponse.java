package com.appearnetworks.aiq.integrationframework.integration;

/**
 * Response to a Client Originated message.
 */
public class COMessageResponse {
    private final boolean success;
    private final Object payload;
    private final int timeToLive;
    private final boolean urgent;
    private final String notificationMessage;
    private final boolean notificationSound;
    private final boolean notificationVibration;

    /**
     * @param success     if the message was successfully processed
     * @param payload     response payload, will be serialized to JSON, see {@link com.fasterxml.jackson.databind.node.ObjectNode}, or {@code null} for no payload
     * @param timeToLive  how long the response to the device will be valid, in seconds. Specify 0 to use default.
     * @param urgent      if the response is urgent and should be delivered to the originating device as soon as possible
     * @param notificationMessage     if the user should be notified with a message when the response reaches his/her device, {@code null} otherwise
     * @param notificationSound       if the user should be notified with a sound when the response reaches his/her device
     *                    (will be ignored if {@code message} is not set)
     * @param notificationVibration   if the user should be notified with a vibration when the response reaches his/her device
     *                    (will be ignored if {@code message} is not set)
     */
    public COMessageResponse(boolean success, Object payload, int timeToLive, boolean urgent, String notificationMessage, boolean notificationSound, boolean notificationVibration) {
        this.success = success;
        this.payload = payload;
        this.timeToLive = timeToLive;
        this.urgent = urgent;
        this.notificationMessage = notificationMessage;
        this.notificationSound = notificationSound;
        this.notificationVibration = notificationVibration;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getPayload() {
        return payload;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public boolean isNotificationSound() {
        return notificationSound;
    }

    public boolean isNotificationVibration() {
        return notificationVibration;
    }
}
