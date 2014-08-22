package com.appearnetworks.aiq.integrationframework.integration;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Base implementation of {@link IntegrationAdapter}.
 * <p/>
 * Contains default implementation of all methods, override the methods you need.
 * <p/>
 * The implementation needs to be thread-safe.
 */
public abstract class IntegrationAdapterBase implements IntegrationAdapter {

    public Collection<DocumentReference> findByUser(String userId) {
        return Collections.emptyList();
    }

    public Object retrieveDocument(String docType, String docId) {
        return null;
    }

    public Attachment retrieveAttachment(String docType, String docId, String name) {
        return null;
    }

    public long insertDocument(String userId, String deviceId, DocumentReference docRef, ObjectNode doc) throws UpdateException {
        throw new UpdateException(HttpStatus.NOT_FOUND);
    }

    public long updateDocument(String userId, String deviceId, DocumentReference docRef, ObjectNode doc) throws UpdateException {
        throw new UpdateException(HttpStatus.NOT_FOUND);
    }

    public void deleteDocument(String userId, String deviceId, DocumentReference docRef) throws UpdateException {
        throw new UpdateException(HttpStatus.NOT_FOUND);
    }

    public DocumentAndAttachmentRevision insertAttachment(String userId, String deviceId, String docType, String docId, String name,
                                                          MediaType contentType, long contentLength, InputStream content)
            throws UpdateException, IOException {
        throw new UpdateException(HttpStatus.NOT_FOUND);
    }

    public DocumentAndAttachmentRevision updateAttachment(String userId, String deviceId, String docType, String docId, String name, long revision,
                                                          MediaType contentType, long contentLength, InputStream content)
            throws UpdateException, IOException {
        throw new UpdateException(HttpStatus.NOT_FOUND);
    }

    public long deleteAttachment(String userId, String deviceId, String docType, String docId, String name, long revision) throws UpdateException {
        throw new UpdateException(HttpStatus.NOT_FOUND);
    }

    public ObjectNode createClientSession(String userId, String deviceId, String sessionId, ObjectNode clientSession) {
        return null;
    }

    public void updateClientSession(String userId, String deviceId, String sessionId, ObjectNode clientSession) {
        // do nothing
    }

    public void removeClientSession(String userId, String deviceId, String sessionId) {
        // do nothing
    }

    public void logout(String userId) {
        // do nothing
    }

    public COMessageResponse processMessage(String destination, COMessage message, FileItemIterator attachments)
            throws UnavailableException, IOException, FileUploadException {
        while (attachments.hasNext()) attachments.next();
        return null;
    }
}
