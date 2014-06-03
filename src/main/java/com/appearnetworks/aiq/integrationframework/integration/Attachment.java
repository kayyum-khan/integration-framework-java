package com.appearnetworks.aiq.integrationframework.integration;

import org.springframework.http.MediaType;

import java.io.InputStream;

/**
 * Attachment associated to a business document.
 */
public final class Attachment {
    public final MediaType contentType;
    public final long contentLength;
    public final InputStream data;
    public final long revision;

    /**
     * @param contentType    content type of attachment
     * @param contentLength  length of attachment
     * @param stream         input stream to the attachment, will be closed after reading
     * @param revision       current revision of the attachment
     */
    public Attachment(MediaType contentType, long contentLength, InputStream stream, long revision) {
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.data = stream;
        this.revision = revision;
    }
}
