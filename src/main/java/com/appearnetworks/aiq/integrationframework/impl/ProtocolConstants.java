package com.appearnetworks.aiq.integrationframework.impl;

public final class ProtocolConstants {
    // Standard HTTP headers
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String IF_MATCH = "If-Match";
    public static final String RETRY_AFTER = "Retry-After";

    // Custom HTTP headers
    public static final String X_AIQ_DOC_REV = "X-AIQ-DocRev";
    public static final String X_AIQ_USER_ID = "X-AIQ-UserId";
    public static final String X_AIQ_DEVICE_ID = "X-AIQ-DeviceId";
    public static final String X_AIQ_MESSAGE_ID = "X-AIQ-MessageId";
    public static final String X_AIQ_CREATED = "X-AIQ-Created";
    public static final String X_AIQ_SUCCESS = "X-AIQ-Success";
    public static final String X_AIQ_TIMETOLIVE = "X-AIQ-TimeToLive";
    public static final String X_AIQ_URGENT = "X-AIQ-Urgent";
    public static final String X_AIQ_NOTIFICATION_SOUND = "X-AIQ-NotificationSound";
    public static final String X_AIQ_NOTIFICATION_VIBRATION = "X-AIQ-NotificationVibration";
    public static final String X_AIQ_NOTIFICATION_MESSAGE = "X-AIQ-NotificationMessage";

    // HTTP header value
    public static final String TRUE = "true";

    // Document types
    public static final String CLIENT_SESSION_DOC_TYPE = "_clientsession";

    // Multipart names
    public static final String PAYLOAD = "_payload";
    public static final String CONTEXT = "_context";
    public static final String MESSAGE = "_message";

    private ProtocolConstants() { }
}
