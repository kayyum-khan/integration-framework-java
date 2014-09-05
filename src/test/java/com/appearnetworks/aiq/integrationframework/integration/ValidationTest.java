package com.appearnetworks.aiq.integrationframework.integration;

import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.Collections;

public class ValidationTest {

    @Test
    public void validBusinessDocument() {
        new MyDocument("foo", "bar", 1, "hoo");
    }

    @Test
    public void validBusinessDocumentWithAttachment() {
        new MyDocumentWithAttachment("foo", "bar", 1,
                Collections.singletonMap("image", new AttachmentReference(1, MediaType.APPLICATION_OCTET_STREAM)), "hoo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDocumentIdForBusinessDocument() {
        new MyDocument("åäö", "foo", 1, "hoo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDocumentTypeForBusinessDocument() {
        new MyDocument("foo", "åäö", 1, "hoo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidAttachmentNameForBusinessDocument() {
        new MyDocumentWithAttachment("foo", "bar", 1,
                Collections.singletonMap("åäö", new AttachmentReference(1, MediaType.APPLICATION_OCTET_STREAM)), "hoo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooLongAttachmentNameForBusinessDocument() {
        new MyDocumentWithAttachment("foo", "bar", 1,
                Collections.singletonMap(
                        "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                        new AttachmentReference(1, MediaType.APPLICATION_OCTET_STREAM)), "hoo");
    }
}
