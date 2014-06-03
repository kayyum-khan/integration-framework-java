package com.appearnetworks.aiq.integrationframework.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Dummy integration adapter for testing.
 */
@Component
public class DummyIntegrationAdapter implements IntegrationAdapter {
    public static final Logger LOG = Logger.getLogger(DummyIntegrationAdapter.class.getName());

    private static final byte[] ATTACHMENT_DATA = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<DocumentReference> findByUserAndDevice(String userId, String deviceId) {
        LOG.info("listDocuments: userId=" + userId + ", deviceId=" + deviceId);
        return Arrays.asList(
                new DocumentReference("one", "MyDoc", 1),
                new DocumentReference("two", "MyDoc", 2),
                new DocumentReference("three", "MyDoc", 3)
        );
    }

    @Override
    public Object retrieveDocument(String docType, String docId) {
        LOG.info("retrieveDocument: type=" + docType + "  id=" + docId);
        switch (docType) {
            case "Found":
                ObjectNode json = mapper.createObjectNode();
                json.put("_id", docId);
                json.put("_rev", 1);
                json.put("_type", docType);
                json.put("foo", "bar");
                return json;

            case "Error":
                throw new RuntimeException("Unexpected doctype");

            default:
                return null;
        }
    }

    @Override
    public Attachment retrieveAttachment(String docType, String docId, String name) {
        LOG.info("retrieveAttachment: docType=" + docType + " docId=" + docId + " name=" + name);

        switch (docType) {
            case "Found":
                return new Attachment(MediaType.valueOf("image/png"), ATTACHMENT_DATA.length, new ByteArrayInputStream(ATTACHMENT_DATA), 1);

            case "Error":
                throw new RuntimeException("Unexpected doctype");

            default:
                return null;
        }
    }

    @Override
    public long insertDocument(String userId, String deviceId, DocumentReference docRef, ObjectNode doc) throws UpdateException {
        try {
            LOG.info("insertDocument: userId=" + userId + " deviceId=" + deviceId + " docRef=" + docRef + "\n" + mapper.writeValueAsString(doc));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        switch (docRef._type) {
            case "Successful":
                return docRef._rev + 1;
            default:
                throw new UpdateException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public long updateDocument(String userId, String deviceId, DocumentReference docRef, ObjectNode doc) throws UpdateException {
        try {
            LOG.info("updateDocument: userId=" + userId + " deviceId=" + deviceId + " docRef=" + docRef + "\n" + mapper.writeValueAsString(doc));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        switch (docRef._type) {
            case "Successful":
                return docRef._rev + 1;

            default:
                throw new UpdateException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteDocument(String userId, String deviceId, DocumentReference docRef) throws UpdateException {
        LOG.info("deleteDocument: userId=" + userId + " deviceId=" + deviceId + " docRef=" + docRef);
        switch (docRef._type) {
            case "Successful":
                return;

            default:
                throw new UpdateException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ObjectNode createClientSession(String userId, String deviceId, String sessionId, ObjectNode clientSession) throws UpdateException {
        try {
            LOG.info("createClientSession: userId=" + userId + " deviceId=" + deviceId + " sessionId=" + sessionId + "\n" + mapper.writeValueAsString(clientSession));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void updateClientSession(String userId, String deviceId, String sessionId, ObjectNode clientSession) throws UpdateException {
        try {
            LOG.info("updateClientSession: userId=" + userId + " deviceId=" + deviceId + " sessionId=" + sessionId + "\n" + mapper.writeValueAsString(clientSession));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeClientSession(String userId, String deviceId, String sessionId) throws UpdateException {
        LOG.info("deleteClientSession: userId=" + userId + " deviceId=" + deviceId + " sessionId=" + sessionId);
    }

    @Override
    public DocumentAndAttachmentRevision insertAttachment(String userId, String deviceId, String docType, String docId, String name,
                                                          MediaType contentType, long contentLength, InputStream content)
            throws UpdateException, IOException {
        LOG.info("insertAttachment: userId=" + userId + " deviceId=" + deviceId + " docType=" + docType + " docId=" + docId + " name=" + name + " contentType=" + contentType + " length=" + contentLength);
        content.close();
        switch (docType) {
            case "Successful":
                return new DocumentAndAttachmentRevision(2, 1);

            default:
                throw new UpdateException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public DocumentAndAttachmentRevision updateAttachment(String userId, String deviceId, String docType, String docId, String name, long revision,
                                                          MediaType contentType, long contentLength, InputStream content)
            throws UpdateException, IOException {
        LOG.info("updateAttachment: userId=" + userId + " deviceId=" + deviceId + " docType=" + docType + " docId=" + docId + " name=" + name + " revision=" + revision + " contentType=" + contentType + " length=" + contentLength);
        content.close();
        switch (docType) {
            case "Successful":
                return new DocumentAndAttachmentRevision(2, revision + 1);

            default:
                throw new UpdateException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public long deleteAttachment(String userId, String deviceId, String docType, String docId, String name, long revision) throws UpdateException {
        LOG.info("deleteAttachment: userId=" + userId + " deviceId=" + deviceId + " docType=" + docType + " docId=" + docId + " name=" + name + " revision=" + revision);
        switch (docType) {
            case "Successful":
                return 2;

            default:
                throw new UpdateException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void logout(String userId) {
        LOG.info("logout: userId=" + userId);
    }

    @Override
    public COMessageResponse processMessage(String destination, COMessage message, FileItemIterator attachments)
            throws UnavailableException, IOException, FileUploadException {
        LOG.info("CO message to " + destination + ": " + message.toString());
        while (attachments.hasNext()) attachments.next();
        return null;
    }
}
