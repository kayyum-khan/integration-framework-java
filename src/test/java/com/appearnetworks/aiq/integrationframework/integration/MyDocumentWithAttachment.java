package com.appearnetworks.aiq.integrationframework.integration;

import java.util.Map;

public class MyDocumentWithAttachment extends BusinessDocument {
    private String foo;

    public MyDocumentWithAttachment() { }

    public MyDocumentWithAttachment(String _id, String _type, long _rev, Map<String, AttachmentReference> _attachments, String foo) {
        super(_id, _type, _rev, _attachments);
        this.foo = foo;
    }

    public String getFoo() {
        return foo;
    }
}
