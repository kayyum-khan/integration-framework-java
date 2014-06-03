package com.appearnetworks.aiq.integrationframework.integration;

import com.appearnetworks.aiq.integrationframework.impl.PlatformRegistrator;
import com.appearnetworks.aiq.integrationframework.impl.integration.IntegrationProtocol;
import com.appearnetworks.aiq.integrationframework.impl.integration.LogoutRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static com.appearnetworks.aiq.integrationframework.impl.ProtocolConstants.*;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"/META-INF/testApplicationContext.xml"})
public class IntegrationProtocolTest {
    private static final String USER_ID = "SomeUser";
    private static final String DEVICE_ID = "SomeDevice";
    private static final String DOC_ID = "the.doc_id";
    private static final String DOC_TYPE = "some_doc.type";
    private static final String NAME = "my_name.ext";
    private static final MediaType CONTENT_TYPE = MediaType.IMAGE_PNG;
    private static final byte[] DATA = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final String PLATFORM_INTEGRATION_USER = "AIQ8Platform";
    private static final String DESTINATION = "dest";
    private static final String MESSAGE_ID = "msgId";
    private static final String PHOTO = "photo";
    private static final String LOGO = "logo";

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private IntegrationProtocol controller;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private PlatformRegistrator platformRegistrator;

    @Mock
    private IntegrationAdapter integrationAdapterMock;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .defaultRequest(get("/").header("Authorization", authHeaderValue(PLATFORM_INTEGRATION_USER, platformRegistrator.getPassword())))
                .build();

        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(controller, "integrationAdapter", integrationAdapterMock);
    }

    @Test
    public void listDocuments() throws Exception {
        when(integrationAdapterMock.findByUserAndDevice(anyString(), anyString())).thenReturn(Arrays.asList(
                new DocumentReference("one", "MyDoc", 1),
                new DocumentReference("two", "MyDoc", 2)));

        mockMvc.perform(get("/aiq/integration/datasync").param("userId", USER_ID).param("deviceId", DEVICE_ID)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.documentReferences[0]._id").value("one"))
                .andExpect(jsonPath("$.documentReferences[1]._id").value("two"));

        verify(integrationAdapterMock).findByUserAndDevice(USER_ID, DEVICE_ID);
    }

    @Test
    public void listDocumentsNoDevice() throws Exception {
        when(integrationAdapterMock.findByUserAndDevice(anyString(), anyString())).thenReturn(Arrays.asList(
                new DocumentReference("one", "MyDoc", 1),
                new DocumentReference("two", "MyDoc", 2)));

        mockMvc.perform(get("/aiq/integration/datasync").param("userId", USER_ID)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.documentReferences[0]._id").value("one"))
                .andExpect(jsonPath("$.documentReferences[1]._id").value("two"));

        verify(integrationAdapterMock).findByUserAndDevice(USER_ID, null);
    }

    @Test
    public void listDocumentsNoDeviceNorUser() throws Exception {
        when(integrationAdapterMock.findByUserAndDevice(anyString(), anyString())).thenReturn(Arrays.asList(
                new DocumentReference("one", "MyDoc", 1)));

        mockMvc.perform(get("/aiq/integration/datasync")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.documentReferences[0]._id").value("one"));

        verify(integrationAdapterMock).findByUserAndDevice(null, null);
    }

    @Test
    public void retrieveDocumentFound() throws Exception {
        long revision = 5;
        ObjectNode expectedJson = mapper.createObjectNode();
        expectedJson.put("_id", DOC_ID);
        expectedJson.put("_rev", (int) revision); // we have to cast to int since Jackson ObjectNode doesn't consider (int)1 equal to (long)1
        expectedJson.put("_type", DOC_TYPE);
        expectedJson.put("foo", "bar");

        when(integrationAdapterMock.retrieveDocument(anyString(), anyString())).thenReturn(expectedJson);

        mockMvc.perform(get("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"" + revision + "\""))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(expectedJson)));

        verify(integrationAdapterMock).retrieveDocument(DOC_TYPE, DOC_ID);
    }

    @Test
    public void businessDocumentWithoutAttachment() throws Exception {
        long revision = 5;
        ObjectNode expectedJson = mapper.createObjectNode();
        expectedJson.put("_id", DOC_ID);
        expectedJson.put("_rev", revision);
        expectedJson.put("_type", DOC_TYPE);
        expectedJson.put("foo", "bar");

        MyDocument doc = new MyDocument(DOC_ID, DOC_TYPE, revision, "bar");

        JsonNode actualJson = mapper.valueToTree(doc);
        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    public void businessDocumentWithAttachment() throws Exception {
        long revision = 5;
        ObjectNode expectedJson = mapper.createObjectNode();
        expectedJson.put("_id", DOC_ID);
        expectedJson.put("_rev", revision);
        expectedJson.put("_type", DOC_TYPE);
        expectedJson.put("foo", "bar");

        ObjectNode attachments = mapper.createObjectNode();
        ObjectNode attachmentNode = mapper.createObjectNode();
        attachmentNode.put("_rev", 1L);
        attachmentNode.put("content_type", "image/png");
        attachments.put("image", attachmentNode);
        expectedJson.put("_attachments", attachments);

        MyDocumentWithAttachment doc = new MyDocumentWithAttachment(DOC_ID, DOC_TYPE, revision,
                Collections.singletonMap("image", new AttachmentReference(1, MediaType.valueOf("image/png"))),
                "bar");

        JsonNode actualJson = mapper.valueToTree(doc);
        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    public void retrieveDocumentNotFound() throws Exception {
        when(integrationAdapterMock.retrieveDocument(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(get("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID)
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void retrieveAttachmentFound() throws Exception {
        MediaType contentType = MediaType.valueOf("application/pdf");
        long revision = 7;

        when(integrationAdapterMock.retrieveAttachment(anyString(), anyString(), anyString())).thenReturn(
                new Attachment(contentType, DATA.length, new ByteArrayInputStream(DATA), revision));

        mockMvc.perform(get("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID + "/" + NAME))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"" + revision + "\""))
                .andExpect(header().string("Content-Length", String.valueOf(DATA.length)))
                .andExpect(content().contentType(contentType))
                .andExpect(content().bytes(DATA));

        verify(integrationAdapterMock).retrieveAttachment(DOC_TYPE, DOC_ID, NAME);
    }

    @Test
    public void retrieveAttachmentNotFound() throws Exception {
        when(integrationAdapterMock.retrieveAttachment(anyString(), anyString(), anyString())).thenReturn(null);

        mockMvc.perform(get("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID + "/" + NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    public void insertDocumentSuccess() throws Exception {
        long initialRevision = 1;

        when(integrationAdapterMock.insertDocument(anyString(), anyString(), any(DocumentReference.class), any(ObjectNode.class))).thenReturn(initialRevision);

        ObjectNode doc = mapper.createObjectNode();
        doc.put("_id", DOC_ID);
        doc.put("_type", DOC_TYPE);
        doc.put("foo", "bar");

        mockMvc.perform(put("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(doc)))
                .andExpect(status().isCreated())
                .andExpect(header().string("ETag", "\"" + initialRevision + "\""));

        verify(integrationAdapterMock).insertDocument(USER_ID, DEVICE_ID, new DocumentReference(DOC_ID, DOC_TYPE, 0), doc);
    }

    @Test
    public void insertDocumentFailed() throws Exception {
        when(integrationAdapterMock.insertDocument(anyString(), anyString(), any(DocumentReference.class), any(ObjectNode.class))).thenThrow(
                new UpdateException(HttpStatus.CONFLICT));

        ObjectNode doc = mapper.createObjectNode();
        doc.put("_id", DOC_ID);
        doc.put("_type", DOC_TYPE);
        doc.put("foo", "bar");

        mockMvc.perform(put("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(doc)))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateDocumentSuccess() throws Exception {
        long revision = 1;
        long newRevision = 2;

        when(integrationAdapterMock.updateDocument(anyString(), anyString(), any(DocumentReference.class), any(ObjectNode.class))).thenReturn(newRevision);

        ObjectNode doc = mapper.createObjectNode();
        doc.put("_id", DOC_ID);
        doc.put("_type", DOC_TYPE);
        doc.put("_rev", (int) revision); // we have to cast to int since Jackson ObjectNode doesn't consider (int)1 equal to (long)1
        doc.put("foo", "BAR");

        mockMvc.perform(put("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(revision))
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(doc)))
                .andExpect(status().isNoContent())
                .andExpect(header().string("ETag", "\"" + newRevision + "\""));

        verify(integrationAdapterMock).updateDocument(USER_ID, DEVICE_ID, new DocumentReference(DOC_ID, DOC_TYPE, revision), doc);
    }

    @Test
    public void updateDocumentFailed() throws Exception {
        long revision = 1;

        when(integrationAdapterMock.updateDocument(anyString(), anyString(), any(DocumentReference.class), any(ObjectNode.class))).thenThrow(
                new UpdateException(HttpStatus.PRECONDITION_FAILED));

        ObjectNode doc = mapper.createObjectNode();
        doc.put("_id", DOC_ID);
        doc.put("_type", DOC_TYPE);
        doc.put("_rev", revision);
        doc.put("foo", "BAR");

        mockMvc.perform(put("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(revision))
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(doc)))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    public void deleteDocumentSuccess() throws Exception {
        long revision = 1;

        mockMvc.perform(delete("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(revision)))
                .andExpect(status().isNoContent());

        verify(integrationAdapterMock).deleteDocument(USER_ID, DEVICE_ID, new DocumentReference(DOC_ID, DOC_TYPE, revision));
    }

    @Test
    public void deleteDocumentFailed() throws Exception {
        long revision = 1;

        doThrow(new UpdateException(HttpStatus.PRECONDITION_FAILED)).when(integrationAdapterMock)
                .deleteDocument(anyString(), anyString(), any(DocumentReference.class));

        mockMvc.perform(delete("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(revision)))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    public void insertAttachmentSuccess() throws Exception {
        long updatedDocumentRevision = 2;
        long initialAttachmentRevision = 1;

        when(integrationAdapterMock.insertAttachment(anyString(), anyString(), anyString(), anyString(), anyString(), any(MediaType.class), anyLong(), any(InputStream.class))).thenReturn(
                new DocumentAndAttachmentRevision(updatedDocumentRevision, initialAttachmentRevision));

        mockMvc.perform(put("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID + "/" + NAME)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("Content-Length", String.valueOf(DATA.length))
                .contentType(CONTENT_TYPE)
                .content(DATA))
                .andExpect(status().isCreated())
                .andExpect(header().string("ETag", "\"" + initialAttachmentRevision + "\""))
                .andExpect(header().string(X_AIQ_DOC_REV, "\"" + updatedDocumentRevision + "\""));

        verify(integrationAdapterMock).insertAttachment(eq(USER_ID), eq(DEVICE_ID), eq(DOC_TYPE), eq(DOC_ID), eq(NAME),
                eq(CONTENT_TYPE), eq((long) DATA.length), any(InputStream.class));
    }

    @Test
    public void insertAttachmentFailed() throws Exception {
        when(integrationAdapterMock.insertAttachment(anyString(), anyString(), anyString(), anyString(), anyString(), any(MediaType.class), anyLong(), any(InputStream.class))).thenThrow(
                new UpdateException(HttpStatus.CONFLICT));

        mockMvc.perform(put("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID + "/" + NAME)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("Content-Length", String.valueOf(DATA.length))
                .contentType(CONTENT_TYPE)
                .content(DATA))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateAttachmentSuccess() throws Exception {
        long revision = 1;
        long updatedDocumentRevision = 2;
        long updatedAttachmentRevision = 1;

        when(integrationAdapterMock.updateAttachment(anyString(), anyString(), anyString(), anyString(), anyString(), anyLong(),
                any(MediaType.class), anyLong(), any(InputStream.class))).thenReturn(
                new DocumentAndAttachmentRevision(updatedDocumentRevision, updatedAttachmentRevision));

        mockMvc.perform(put("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID + "/" + NAME)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(revision))
                .header("Content-Length", String.valueOf(DATA.length))
                .contentType(CONTENT_TYPE)
                .content(DATA))
                .andExpect(status().isNoContent())
                .andExpect(header().string("ETag", "\"" + updatedAttachmentRevision + "\""))
                .andExpect(header().string(X_AIQ_DOC_REV, "\"" + updatedDocumentRevision + "\""));

        verify(integrationAdapterMock).updateAttachment(eq(USER_ID), eq(DEVICE_ID), eq(DOC_TYPE), eq(DOC_ID), eq(NAME), eq(revision),
                eq(CONTENT_TYPE), eq((long) DATA.length), any(InputStream.class));
    }

    @Test
    public void updateAttachmentFailed() throws Exception {
        long revision = 1;

        when(integrationAdapterMock.updateAttachment(anyString(), anyString(), anyString(), anyString(), anyString(), anyLong(),
                any(MediaType.class), anyLong(), any(InputStream.class))).thenThrow(
                new UpdateException(HttpStatus.PRECONDITION_FAILED));

        mockMvc.perform(put("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID + "/" + NAME)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(revision))
                .header("Content-Length", String.valueOf(DATA.length))
                .contentType(CONTENT_TYPE)
                .content(DATA))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    public void deleteAttachmentSuccess() throws Exception {
        long revision = 1;
        long updatedDocumentRevision = 2;

        when(integrationAdapterMock.deleteAttachment(anyString(), anyString(), anyString(), anyString(), anyString(), anyLong())).thenReturn(
                updatedDocumentRevision);

        mockMvc.perform(delete("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID + "/" + NAME)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(revision)))
                .andExpect(status().isNoContent())
                .andExpect(header().string(X_AIQ_DOC_REV, "\"" + updatedDocumentRevision + "\""));

        verify(integrationAdapterMock).deleteAttachment(USER_ID, DEVICE_ID, DOC_TYPE, DOC_ID, NAME, revision);
    }

    @Test
    public void deleteAttachmentFailed() throws Exception {
        long revision = 1;

        when(integrationAdapterMock.deleteAttachment(anyString(), anyString(), anyString(), anyString(), anyString(), anyLong())).thenThrow(
                new UpdateException(HttpStatus.PRECONDITION_FAILED));

        mockMvc.perform(delete("/aiq/integration/datasync/" + DOC_TYPE + "/" + DOC_ID + "/" + NAME)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(revision)))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    public void createClientSessionObjectNode() throws Exception {
        ObjectNode initialBackendContext = mapper.createObjectNode();
        initialBackendContext.put("com.example.provider", "test");

        when(integrationAdapterMock.createClientSession(anyString(), anyString(), anyString(), any(ObjectNode.class))).thenReturn(initialBackendContext);

        ObjectNode doc = mapper.createObjectNode();
        doc.put("_id", DOC_ID);
        doc.put("_type", CLIENT_SESSION_DOC_TYPE);
        doc.put("foo", "bar");

        mockMvc.perform(put("/aiq/integration/datasync/" + CLIENT_SESSION_DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(doc)))
                .andExpect(status().isCreated())
                .andExpect(header().string("ETag", "\"" + 1 + "\""))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(initialBackendContext)));

        verify(integrationAdapterMock).createClientSession(USER_ID, DEVICE_ID, DOC_ID, doc);
    }

    @Test
    public void createClientSessionNull() throws Exception {
        when(integrationAdapterMock.createClientSession(anyString(), anyString(), anyString(), any(ObjectNode.class))).thenReturn(null);

        ObjectNode doc = mapper.createObjectNode();
        doc.put("_id", DOC_ID);
        doc.put("_type", CLIENT_SESSION_DOC_TYPE);
        doc.put("foo", "bar");

        mockMvc.perform(put("/aiq/integration/datasync/" + CLIENT_SESSION_DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(doc)))
                .andExpect(status().isCreated())
                .andExpect(header().string("ETag", "\"" + 1 + "\""))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(integrationAdapterMock).createClientSession(USER_ID, DEVICE_ID, DOC_ID, doc);
    }

    @Test
    public void updateClientSession() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        doc.put("_id", DOC_ID);
        doc.put("_type", CLIENT_SESSION_DOC_TYPE);
        doc.put("foo", "bar");

        long initialRevision = 1;

        mockMvc.perform(put("/aiq/integration/datasync/" + CLIENT_SESSION_DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(initialRevision))
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(doc)))
                .andExpect(status().isNoContent())
                .andExpect(header().string("ETag", "\"" + (initialRevision + 1) + "\""));

        verify(integrationAdapterMock).updateClientSession(USER_ID, DEVICE_ID, DOC_ID, doc);
    }

    @Test
    public void removeClientSession() throws Exception {
        long initialRevision = 1;

        mockMvc.perform(delete("/aiq/integration/datasync/" + CLIENT_SESSION_DOC_TYPE + "/" + DOC_ID)
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header("If-Match", makeETag(initialRevision)))
                .andExpect(status().isNoContent());

        verify(integrationAdapterMock).removeClientSession(USER_ID, DEVICE_ID, DOC_ID);
    }

    @Test
    public void logout() throws Exception {
        mockMvc.perform(post("/aiq/integration/logout")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(new LogoutRequest(USER_ID))))
                .andExpect(status().isNoContent());

        verify(integrationAdapterMock).logout(USER_ID);
    }

    @Test
    public void heartbeat() throws Exception {
        mockMvc.perform(get("/aiq/integration/heartbeat"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string("{}"));
    }

    @Test
    public void coMessageSuccess() throws Exception {
        long created = System.currentTimeMillis();
        String notification = "notification";
        byte[] photo = new byte[]{0, 1, 2, 6, 76};
        byte[] logo = new byte[]{11, 56, 45, 43, 17};

        ObjectNode payload = mapper.createObjectNode();
        payload.put("foo", "bar");
        payload.put("baz", "apa");

        ObjectNode context = mapper.createObjectNode();
        context.put("hoo", "xxx");
        context.put("boo", "yyy");

        ObjectNode response = mapper.createObjectNode();
        response.put("status", "OK");

        when(integrationAdapterMock.processMessage(anyString(), any(COMessage.class), any(FileItemIterator.class))).thenReturn(
                new COMessageResponse(true, response, 3600, true, notification, true, true)
        );

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody(PAYLOAD, mapper.writeValueAsBytes(payload), ContentType.APPLICATION_JSON, null)
                .addBinaryBody(CONTEXT, mapper.writeValueAsBytes(context), ContentType.APPLICATION_JSON, null)
                .addBinaryBody(PHOTO, photo, ContentType.create("image/jpeg"), null)
                .addBinaryBody(LOGO, logo, ContentType.create("image/png"), null)
                .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entity.writeTo(buffer);

        mockMvc.perform(post("/aiq/integration/comessage/" + DESTINATION)
                .contentType(MediaType.parseMediaType(entity.getContentType().getValue()))
                .content(buffer.toByteArray())
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header(X_AIQ_MESSAGE_ID, MESSAGE_ID)
                .header(X_AIQ_CREATED, String.valueOf(created)))
                .andExpect(status().isOk())
                .andExpect(header().string(X_AIQ_SUCCESS, "true"))
                .andExpect(header().string(X_AIQ_TIMETOLIVE, "3600"))
                .andExpect(header().string(X_AIQ_URGENT, TRUE))
                .andExpect(header().string(X_AIQ_NOTIFICATION_MESSAGE, notification))
                .andExpect(header().string(X_AIQ_NOTIFICATION_SOUND, TRUE))
                .andExpect(header().string(X_AIQ_NOTIFICATION_VIBRATION, TRUE))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(response)));

        ArgumentCaptor<COMessage> message = ArgumentCaptor.forClass(COMessage.class);
        ArgumentCaptor<FileItemIterator> attachments = ArgumentCaptor.forClass(FileItemIterator.class);
        verify(integrationAdapterMock).processMessage(eq(DESTINATION), message.capture(), attachments.capture());
        assertEquals(MESSAGE_ID, message.getValue().getMessageId());
        assertEquals(USER_ID, message.getValue().getUserId());
        assertEquals(DEVICE_ID, message.getValue().getDeviceId());
        assertEquals(new Date(created), message.getValue().getCreated());
        assertEquals(payload, message.getValue().getPayload());
        assertEquals(context, message.getValue().getContext());

        FileItemStream _photo = attachments.getValue().next();
        assertEquals(PHOTO, _photo.getFieldName());
        assertEquals(IMAGE_JPEG_VALUE, _photo.getContentType());
        assertArrayEquals(photo, FileCopyUtils.copyToByteArray(_photo.openStream()));

        FileItemStream _logo = attachments.getValue().next();
        assertEquals(LOGO, _logo.getFieldName());
        assertEquals(IMAGE_PNG_VALUE, _logo.getContentType());
        assertArrayEquals(logo, FileCopyUtils.copyToByteArray(_logo.openStream()));

        assertFalse(attachments.getValue().hasNext());
    }

    @Test
    public void coMessageFailure() throws Exception {
        long created = System.currentTimeMillis();

        ObjectNode payload = mapper.createObjectNode();
        payload.put("foo", "bar");
        payload.put("baz", "apa");

        ObjectNode context = mapper.createObjectNode();
        context.put("hoo", "xxx");
        context.put("boo", "yyy");

        ObjectNode response = mapper.createObjectNode();
        response.put("status", "ERROR");

        when(integrationAdapterMock.processMessage(anyString(), any(COMessage.class), any(FileItemIterator.class))).thenReturn(
                new COMessageResponse(false, response, 0, false, null, false, false)
        );

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody(PAYLOAD, mapper.writeValueAsBytes(payload), ContentType.APPLICATION_JSON, null)
                .addBinaryBody(CONTEXT, mapper.writeValueAsBytes(context), ContentType.APPLICATION_JSON, null)
                .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entity.writeTo(buffer);

        mockMvc.perform(post("/aiq/integration/comessage/" + DESTINATION)
                .contentType(MediaType.parseMediaType(entity.getContentType().getValue()))
                .content(buffer.toByteArray())
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header(X_AIQ_MESSAGE_ID, MESSAGE_ID)
                .header(X_AIQ_CREATED, String.valueOf(created)))
                .andExpect(status().isOk())
                .andExpect(header().string(X_AIQ_SUCCESS, "false"))
                .andExpect(header().string(X_AIQ_TIMETOLIVE, (String) null))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(response)));

        ArgumentCaptor<COMessage> message = ArgumentCaptor.forClass(COMessage.class);
        verify(integrationAdapterMock).processMessage(eq(DESTINATION), message.capture(), any(FileItemIterator.class));
        assertEquals(MESSAGE_ID, message.getValue().getMessageId());
        assertEquals(USER_ID, message.getValue().getUserId());
        assertEquals(DEVICE_ID, message.getValue().getDeviceId());
        assertEquals(new Date(created), message.getValue().getCreated());
        assertEquals(payload, message.getValue().getPayload());
        assertEquals(context, message.getValue().getContext());
    }

    @Test
    public void coMessageNoResponse() throws Exception {
        long created = System.currentTimeMillis();

        ObjectNode payload = mapper.createObjectNode();
        payload.put("foo", "bar");
        payload.put("baz", "apa");

        ObjectNode context = mapper.createObjectNode();
        context.put("hoo", "xxx");
        context.put("boo", "yyy");

        when(integrationAdapterMock.processMessage(anyString(), any(COMessage.class), any(FileItemIterator.class))).thenReturn(
                null
        );

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody(PAYLOAD, mapper.writeValueAsBytes(payload), ContentType.APPLICATION_JSON, null)
                .addBinaryBody(CONTEXT, mapper.writeValueAsBytes(context), ContentType.APPLICATION_JSON, null)
                .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entity.writeTo(buffer);

        mockMvc.perform(post("/aiq/integration/comessage/" + DESTINATION)
                .contentType(MediaType.parseMediaType(entity.getContentType().getValue()))
                .content(buffer.toByteArray())
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header(X_AIQ_MESSAGE_ID, MESSAGE_ID)
                .header(X_AIQ_CREATED, String.valueOf(created)))
                .andExpect(status().isNoContent());

        ArgumentCaptor<COMessage> message = ArgumentCaptor.forClass(COMessage.class);
        verify(integrationAdapterMock).processMessage(eq(DESTINATION), message.capture(), any(FileItemIterator.class));
        assertEquals(MESSAGE_ID, message.getValue().getMessageId());
        assertEquals(USER_ID, message.getValue().getUserId());
        assertEquals(DEVICE_ID, message.getValue().getDeviceId());
        assertEquals(new Date(created), message.getValue().getCreated());
        assertEquals(payload, message.getValue().getPayload());
        assertEquals(context, message.getValue().getContext());
    }

    @Test
    public void coMessageUnavailable() throws Exception {
        long created = System.currentTimeMillis();

        ObjectNode payload = mapper.createObjectNode();
        payload.put("foo", "bar");
        payload.put("baz", "apa");

        ObjectNode context = mapper.createObjectNode();
        context.put("hoo", "xxx");
        context.put("boo", "yyy");

        when(integrationAdapterMock.processMessage(anyString(), any(COMessage.class), any(FileItemIterator.class))).thenThrow(new UnavailableException());

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody(PAYLOAD, mapper.writeValueAsBytes(payload), ContentType.APPLICATION_JSON, null)
                .addBinaryBody(CONTEXT, mapper.writeValueAsBytes(context), ContentType.APPLICATION_JSON, null)
                .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entity.writeTo(buffer);

        mockMvc.perform(post("/aiq/integration/comessage/" + DESTINATION)
                .contentType(MediaType.parseMediaType(entity.getContentType().getValue()))
                .content(buffer.toByteArray())
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header(X_AIQ_MESSAGE_ID, MESSAGE_ID)
                .header(X_AIQ_CREATED, String.valueOf(created)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void coMessageRetryAfter() throws Exception {
        long created = System.currentTimeMillis();

        int retryAfter = 4711;

        ObjectNode payload = mapper.createObjectNode();
        payload.put("foo", "bar");
        payload.put("baz", "apa");

        ObjectNode context = mapper.createObjectNode();
        context.put("hoo", "xxx");
        context.put("boo", "yyy");

        when(integrationAdapterMock.processMessage(anyString(), any(COMessage.class), any(FileItemIterator.class))).thenThrow(new UnavailableException(retryAfter));

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody(PAYLOAD, mapper.writeValueAsBytes(payload), ContentType.APPLICATION_JSON, null)
                .addBinaryBody(CONTEXT, mapper.writeValueAsBytes(context), ContentType.APPLICATION_JSON, null)
                .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entity.writeTo(buffer);

        mockMvc.perform(post("/aiq/integration/comessage/" + DESTINATION)
                .contentType(MediaType.parseMediaType(entity.getContentType().getValue()))
                .content(buffer.toByteArray())
                .header(X_AIQ_USER_ID, USER_ID)
                .header(X_AIQ_DEVICE_ID, DEVICE_ID)
                .header(X_AIQ_MESSAGE_ID, MESSAGE_ID)
                .header(X_AIQ_CREATED, String.valueOf(created)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string(RETRY_AFTER, String.valueOf(retryAfter)));
    }

    private String makeETag(long rev) {
        return '\"' + String.valueOf(rev) + '\"';
    }

    protected String authHeaderValue(final String user, final String pass) {
        String auth = user + ":" + pass;
        String encodedAuth = Base64.encodeBase64String(auth.getBytes(Charset.forName("UTF-8")));
        return "Basic " + encodedAuth;
    }
}
