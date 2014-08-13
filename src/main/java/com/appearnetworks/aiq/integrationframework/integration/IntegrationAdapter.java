package com.appearnetworks.aiq.integrationframework.integration;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The behaviour of an integration adapter.
 * <p/>
 * The implementation needs to be thread-safe.
 */
public interface IntegrationAdapter {
    /**
     * Find the set of document that a given user should have.
     * <p/>
     * {@code userId} may be {@code null}, in that case the documents that
     * all users in the organization should have should be returned.
     * <p/>
     * If the user is unrecognized, the response must be as if the {@code userId} parameter is {@code null}.
     *
     * @param userId   the user, may be {@code null}
     * @return document references defining what documents the user should have, possibly empty but never {@code null}
     */
    List<DocumentReference> findByUser(String userId);

    /**
     * Retrieve a single document.
     * <p/>
     * Do not restrict or filter access through this method, the server will only retrieve the documents returned by
     * {@link #findByUser}.
     * <p/>
     * The server may cache documents internally to avoid making multiple calls to this method for the same document.
     * <p/>
     * You can either return an {@link com.fasterxml.jackson.databind.node.ObjectNode} or a subclass of {@link BusinessDocument}.
     *
     * @param docType document type
     * @param docId   document id
     * @return the document, or {@code null} if not found
     */
    Object retrieveDocument(String docType, String docId);

    /**
     * Retrieve an attachment.
     * <p/>
     * Do not restrict or filter access through this method, the server will only retrieve the attachments returned by
     * {@link #retrieveDocument}.
     * <p/>
     * The server may cache attachments internally to avoid making multiple calls to this method for the same attachment.
     *
     * @param docType document type
     * @param docId   document id
     * @param name    attachment name
     * @return the attachment, or {@code null} if not found
     */
    Attachment retrieveAttachment(String docType, String docId, String name);

    /**
     * Insert a document.
     *
     * @param userId   The user who inserted the document
     * @param deviceId The device that the user is using
     * @param docRef   reference to the document to insert (revision will be 0)
     * @param doc      the document to insert
     * @return initial revision, usually 1
     * @throws UpdateException can be thrown if update fails
     */
    long insertDocument(String userId, String deviceId, DocumentReference docRef, ObjectNode doc) throws UpdateException;

    /**
     * Update a document.
     *
     * @param userId   The user who updated the document
     * @param deviceId The device that the user is using
     * @param docRef   reference to the document to update
     * @param doc      the updated document
     * @return updated revision
     * @throws UpdateException can be thrown if update fails
     */
    long updateDocument(String userId, String deviceId, DocumentReference docRef, ObjectNode doc) throws UpdateException;

    /**
     * Delete a document.
     *
     * @param userId   The user who deleted the document
     * @param deviceId The device that the user is using
     * @param docRef   reference to the document to delete
     * @throws UpdateException can be thrown if update fails
     */
    void deleteDocument(String userId, String deviceId, DocumentReference docRef) throws UpdateException;

    /**
     * Insert an attachment.
     *
     * @param userId        The user who inserted the attachment
     * @param deviceId      The device that the user is using
     * @param docType       document type
     * @param docId         document id
     * @param name          attachment name
     * @param contentType   content type of attachment
     * @param contentLength length of attachment, or -1 if unknown
     * @param content       attachment, the stream must be closed before returning
     * @return updated revision of document and initial revision of attachment
     * @throws UpdateException can be thrown if update fails
     */
    DocumentAndAttachmentRevision insertAttachment(String userId, String deviceId, String docType, String docId,
                                                   String name, MediaType contentType, long contentLength, InputStream content)
            throws UpdateException, IOException;

    /**
     * Updates an attachment.
     *
     * @param userId        The user who inserted the attachment
     * @param deviceId      The device that the user is using
     * @param docType       document type
     * @param docId         document id
     * @param name          attachment name
     * @param revision      current revision of attachment
     * @param contentType   content type of attachment
     * @param contentLength length of attachment, or -1 if unknown
     * @param content       attachment, the stream must be closed before returning
     * @return updated revision of document and updated revision of attachment
     * @throws UpdateException can be thrown if update fails
     */
    DocumentAndAttachmentRevision updateAttachment(String userId, String deviceId, String docType, String docId,
                                                   String name, long revision,
                                                   MediaType contentType, long contentLength, InputStream content)
            throws UpdateException, IOException;

    /**
     * Deletes an attachment.
     *
     * @param userId   The user who inserted the attachment
     * @param deviceId The device that the user is using
     * @param docType  document type
     * @param docId    document id
     * @param name     attachment name
     * @param revision current revision of attachment
     * @return updated revision of document
     * @throws UpdateException can be thrown if update fails
     */
    long deleteAttachment(String userId, String deviceId, String docType, String docId, String name, long revision)
            throws UpdateException;

    /**
     * A client session is being created.
     *
     * @param userId        The user who the session belongs to
     * @param deviceId      The device that the user is using
     * @param sessionId     session id
     * @param clientSession the client session
     * @return initial backend context, or {@code null} to not set any initial backend context
     */
    ObjectNode createClientSession(String userId, String deviceId, String sessionId, ObjectNode clientSession);

    /**
     * A client session is being updated.
     *
     * @param userId        The user who the session belongs to
     * @param deviceId      The device that the user is using
     * @param sessionId     session id
     * @param clientSession the client session
     */
    void updateClientSession(String userId, String deviceId, String sessionId, ObjectNode clientSession);

    /**
     * A client session is being removed.
     *
     * @param userId    The user who the session belongs to
     * @param deviceId  The device that the user is using
     * @param sessionId session id
     */
    void removeClientSession(String userId, String deviceId, String sessionId);

    /**
     * Called by the server when an administrative user logs out.
     * The integration adapter is expected to invalidate the session of the user.
     *
     * @param userId user id
     */
    void logout(String userId);


    /**
     * Called by the server to deliver a Client Originated message.
     *
     * @param destination message destination
     * @param message     the message
     * @param attachments attachments, use {@link org.apache.commons.fileupload.FileItemStream#getFieldName()} to retrieve attachment name
     * @return response to send back to originating user/device, or {@code null} to not send any response
     * @throws UnavailableException if temporary unable to process the message, and the server should retry later
     */
    COMessageResponse processMessage(String destination, COMessage message, FileItemIterator attachments)
            throws UnavailableException, IOException, FileUploadException;
}
