package com.appearnetworks.aiq.integrationframework.impl.integration;

import com.appearnetworks.aiq.integrationframework.integration.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static com.appearnetworks.aiq.integrationframework.impl.ProtocolConstants.*;
import static org.springframework.http.HttpStatus.*;

/**
 * Implement the AIQ 8 integration protocol, and delegate the behaviour to an {@link IntegrationAdapter}.
 */
@Controller
@RequestMapping(value = "/aiq/integration")
public class IntegrationProtocol {
    @Autowired
    private IntegrationAdapter integrationAdapter;

    private ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/datasync", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    ListDocumentsResponse listDocuments(@RequestParam(value = "userId", required = false) String userId) {
        return new ListDocumentsResponse(integrationAdapter.findByUser(userId));
    }

    @RequestMapping(value = "/datasync/{docType}/{docId:.*}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectNode> getDocument(@PathVariable("docType") String docType,
                                                  @PathVariable("docId") String docId) {
        Object document = integrationAdapter.retrieveDocument(docType, docId);
        if (document == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            ObjectNode json;
            if (document instanceof ObjectNode) {
                json = (ObjectNode) document;
            } else {
                json = mapper.valueToTree(document);
            }

            long revision = json.get("_rev").asLong();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setETag(makeETag(revision));
            return new ResponseEntity<>(json, responseHeaders, OK);
        }
    }

    @RequestMapping(value = "/datasync/{docType}/{docId}/{name:.*}", method = RequestMethod.GET)
    public void getAttachment(@PathVariable("docType") String docType,
                              @PathVariable("docId") String docId,
                              @PathVariable("name") String name,
                              HttpServletResponse response) throws IOException {
        Attachment attachment = integrationAdapter.retrieveAttachment(docType, docId, name);
        if (attachment == null) {
            response.sendError(HttpStatus.NOT_FOUND.value());
        } else {
            response.setStatus(OK.value());
            response.addHeader("ETag", makeETag(attachment.revision));
            response.setContentType(attachment.contentType.toString());
            response.addHeader(CONTENT_LENGTH, String.valueOf(attachment.contentLength));
            FileCopyUtils.copy(attachment.data, response.getOutputStream());
        }
    }

    @RequestMapping(value = "/datasync/{docType}/{docId:.*}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> insertDocument(@RequestHeader(X_AIQ_USER_ID) String userId,
                                            @RequestHeader(X_AIQ_DEVICE_ID) String deviceId,
                                            @PathVariable("docType") String docType,
                                            @PathVariable("docId") String docId,
                                            @RequestBody ObjectNode doc) {
        try {
            HttpHeaders responseHeaders = new HttpHeaders();
            switch (docType) {
                case CLIENT_SESSION_DOC_TYPE:
                    ObjectNode document = integrationAdapter.createClientSession(userId, deviceId, docId, doc);
                    ObjectNode backendContext;
                    if (document == null) {
                        backendContext = mapper.createObjectNode();
                    } else {
                        backendContext = document;
                    }
                    responseHeaders.setETag(makeETag(1));
                    return new ResponseEntity<>(backendContext, responseHeaders, HttpStatus.CREATED);

                default:
                    long revision = integrationAdapter.insertDocument(userId, deviceId, new DocumentReference(docId, docType, 0), doc);
                    responseHeaders.setETag(makeETag(revision));
                    return new ResponseEntity<Object>(responseHeaders, HttpStatus.CREATED);
            }

        } catch (UpdateException e) {
            return new ResponseEntity<Object>(e.getStatusCode());
        }
    }

    @RequestMapping(value = "/datasync/{docType}/{docId:.*}", method = RequestMethod.PUT, headers = {IF_MATCH}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateDocument(@RequestHeader(X_AIQ_USER_ID) String userId,
                                            @RequestHeader(X_AIQ_DEVICE_ID) String deviceId,
                                            @RequestHeader(IF_MATCH) String ifMatch,
                                            @PathVariable("docType") String docType,
                                            @PathVariable("docId") String docId,
                                            @RequestBody ObjectNode doc) {
        try {
            long currentRevision = parseRevision(ifMatch);
            HttpHeaders responseHeaders = new HttpHeaders();
            switch (docType) {
                case CLIENT_SESSION_DOC_TYPE:
                    integrationAdapter.updateClientSession(userId, deviceId, docId, doc);
                    responseHeaders.setETag(makeETag(currentRevision + 1));
                    return new ResponseEntity<Object>(responseHeaders, NO_CONTENT);

                default:
                    long revision = integrationAdapter.updateDocument(userId, deviceId, new DocumentReference(docId, docType, currentRevision), doc);
                    responseHeaders.setETag(makeETag(revision));
                    return new ResponseEntity<Object>(responseHeaders, NO_CONTENT);
            }
        } catch (UpdateException e) {
            return new ResponseEntity<Object>(e.getStatusCode());
        }
    }

    @RequestMapping(value = "/datasync/{docType}/{docId:.*}", method = RequestMethod.DELETE, headers = {IF_MATCH})
    public ResponseEntity<?> deleteDocument(@RequestHeader(X_AIQ_USER_ID) String userId,
                                            @RequestHeader(X_AIQ_DEVICE_ID) String deviceId,
                                            @RequestHeader(IF_MATCH) String ifMatch,
                                            @PathVariable("docType") String docType,
                                            @PathVariable("docId") String docId) {
        try {
            long currentRevision = parseRevision(ifMatch);
            switch (docType) {
                case CLIENT_SESSION_DOC_TYPE:
                    integrationAdapter.removeClientSession(userId, deviceId, docId);
                    return new ResponseEntity<Object>(NO_CONTENT);

                default:
                    integrationAdapter.deleteDocument(userId, deviceId, new DocumentReference(docId, docType, currentRevision));
                    return new ResponseEntity<Object>(NO_CONTENT);
            }
        } catch (UpdateException e) {
            return new ResponseEntity<Object>(e.getStatusCode());
        }
    }

    @RequestMapping(value = "/datasync/{docType}/{docId}/{name:.*}", method = RequestMethod.PUT)
    public ResponseEntity<Object> insertAttachment(@RequestHeader(X_AIQ_USER_ID) String userId,
                                                   @RequestHeader(X_AIQ_DEVICE_ID) String deviceId,
                                                   @RequestHeader(CONTENT_TYPE) String contentType,
                                                   @RequestHeader(value = CONTENT_LENGTH, required = false, defaultValue = "-1") long contentLength,
                                                   @PathVariable("docType") String docType,
                                                   @PathVariable("docId") String docId,
                                                   @PathVariable("name") String name,
                                                   InputStream body) throws IOException {
        try {
            DocumentAndAttachmentRevision revisions = integrationAdapter.insertAttachment(userId, deviceId,
                    docType, docId, name, MediaType.parseMediaType(contentType), contentLength, body);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(X_AIQ_DOC_REV, makeETag(revisions.documentRev));
            responseHeaders.setETag(makeETag(revisions.attachmentRev));
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch (UpdateException e) {
            return new ResponseEntity<>(e.getStatusCode());
        }
    }

    @RequestMapping(value = "/datasync/{docType}/{docId}/{name:.*}", method = RequestMethod.PUT, headers = {IF_MATCH})
    public ResponseEntity<Object> updateAttachment(@RequestHeader(X_AIQ_USER_ID) String userId,
                                                   @RequestHeader(X_AIQ_DEVICE_ID) String deviceId,
                                                   @RequestHeader(IF_MATCH) String ifMatch,
                                                   @RequestHeader(CONTENT_TYPE) String contentType,
                                                   @RequestHeader(value = CONTENT_LENGTH, required = false, defaultValue = "-1") long contentLength,
                                                   @PathVariable("docType") String docType,
                                                   @PathVariable("docId") String docId,
                                                   @PathVariable("name") String name,
                                                   InputStream body) throws IOException {
        try {
            DocumentAndAttachmentRevision revisions = integrationAdapter.updateAttachment(userId, deviceId,
                    docType, docId, name, parseRevision(ifMatch), MediaType.parseMediaType(contentType), contentLength, body);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(X_AIQ_DOC_REV, makeETag(revisions.documentRev));
            responseHeaders.setETag(makeETag(revisions.attachmentRev));
            return new ResponseEntity<>(responseHeaders, NO_CONTENT);
        } catch (UpdateException e) {
            return new ResponseEntity<>(e.getStatusCode());
        }
    }

    @RequestMapping(value = "/datasync/{docType}/{docId}/{name:.*}", method = RequestMethod.DELETE, headers = {IF_MATCH})
    public ResponseEntity<Object> updateAttachment(@RequestHeader(X_AIQ_USER_ID) String userId,
                                                   @RequestHeader(X_AIQ_DEVICE_ID) String deviceId,
                                                   @RequestHeader(IF_MATCH) String ifMatch,
                                                   @PathVariable("docType") String docType,
                                                   @PathVariable("docId") String docId,
                                                   @PathVariable("name") String name) {
        try {
            long updatedDocumentRevision = integrationAdapter.deleteAttachment(userId, deviceId,
                    docType, docId, name, parseRevision(ifMatch));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(X_AIQ_DOC_REV, makeETag(updatedDocumentRevision));
            return new ResponseEntity<>(responseHeaders, NO_CONTENT);
        } catch (UpdateException e) {
            return new ResponseEntity<>(e.getStatusCode());
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity<Object> logout(@RequestBody LogoutRequest request) {
        integrationAdapter.logout(request.getUserId());
        return new ResponseEntity<>(NO_CONTENT);
    }

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectNode> heartbeat() {
        return new ResponseEntity<>(mapper.createObjectNode(), OK);
    }

    @RequestMapping(value = "/comessage/{destination}", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectNode> coMessage(@PathVariable("destination") String destination,
                                                @RequestHeader(X_AIQ_USER_ID) String userId,
                                                @RequestHeader(X_AIQ_DEVICE_ID) String deviceId,
                                                @RequestHeader(X_AIQ_MESSAGE_ID) String messageId,
                                                @RequestHeader(X_AIQ_CREATED) long created,
                                                HttpServletRequest request) throws IOException, FileUploadException {

        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterator = upload.getItemIterator(request);

        ObjectNode payload = (ObjectNode)mapper.readTree(iterator.next().openStream());
        ObjectNode context = (ObjectNode)mapper.readTree(iterator.next().openStream());

        COMessage message = new COMessage(messageId, userId, deviceId, created, payload, context);

        HttpHeaders headers = new HttpHeaders();
        try {
            COMessageResponse response = integrationAdapter.processMessage(destination, message, iterator);
            if (response != null) {
                headers.add(X_AIQ_SUCCESS, String.valueOf(response.isSuccess()));
                if (response.isUrgent()) headers.add(X_AIQ_URGENT, TRUE);
                if (response.getTimeToLive() > 0) headers.add(X_AIQ_TIMETOLIVE, String.valueOf(response.getTimeToLive()));
                if (response.getNotificationMessage() != null) {
                    headers.add(X_AIQ_NOTIFICATION_MESSAGE, response.getNotificationMessage());
                    if (response.isNotificationSound()) headers.add(X_AIQ_NOTIFICATION_SOUND, TRUE);
                    if (response.isNotificationVibration()) headers.add(X_AIQ_NOTIFICATION_VIBRATION, TRUE);
                }

                if (response.getPayload() == null)
                    return new ResponseEntity<>(headers, OK);
                else if (response.getPayload() instanceof ObjectNode)
                    return new ResponseEntity<>((ObjectNode) response.getPayload(), headers, OK);
                else
                    return new ResponseEntity<>((ObjectNode) mapper.valueToTree(response.getPayload()), headers, OK);
            } else {
                return new ResponseEntity<>(headers, NO_CONTENT);
            }
        } catch (UnavailableException e) {
            if (e.getRetryAfterSeconds() > 0) {
                headers.add(RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()));
            }
            return new ResponseEntity<>(headers, SERVICE_UNAVAILABLE);
        }
    }

    private String makeETag(long rev) {
        return '\"' + String.valueOf(rev) + '\"';
    }

    private long parseRevision(String etag) {
        return Long.parseLong(etag.substring(1, etag.length() - 1));
    }
}
