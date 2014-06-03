package com.appearnetworks.aiq.integrationframework.integration;

import org.springframework.http.MediaType;

/**
 * Reference to an attachment in a business document.
 */
public class AttachmentReference {
    private final long _rev;
    private final String content_type;

    /**
     * @param _rev          attachment revision
     * @param content_type  attachment content type
     */
    public AttachmentReference(long _rev, MediaType content_type) {
        this._rev = _rev;
        this.content_type = content_type.toString();
    }

    public long get_rev() {
        return _rev;
    }

    public String getContent_type() {
        return content_type;
    }
}
