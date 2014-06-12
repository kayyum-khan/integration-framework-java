package com.appearnetworks.aiq.integrationframework.server;

import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Attachment associated with a message.
 */
public class MessageAttachment {
    public final String name;
    public final MediaType contentType;
    public final byte[] data;

    /**
     * @param name           attachment name, must be unique within a message, set to {@code null} to have it auto-generated
     * @param contentType    content type of attachment
     * @param data           attachment data
     */
    public MessageAttachment(String name, MediaType contentType, byte[] data) {
        Assert.notNull(contentType, "contentType cannot be null");
        Assert.notNull(data, "data cannot be null");
        if (name == null) name = UUID.randomUUID().toString();
        this.name = name;
        this.contentType = contentType;
        this.data = data;
    }
}
