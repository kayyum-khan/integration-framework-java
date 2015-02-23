package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/META-INF/testApplicationContext.xml"})
public class BackendMessageTest {
    private static final String PHOTO = "photo";
    private static final String MY_TYPE = "MyType";
    private static final byte[] photo = new byte[]{0, 1, 2, 6, 76};
    private static final byte[] logo = new byte[]{11, 56, 45, 43, 17};

    private final ObjectMapper mapper = new ObjectMapper();

    private ObjectNode payload;

    @Autowired
    private IntegrationService aiqService;

    @Before
    public void setUp() {
        payload = mapper.createObjectNode();
        payload.put("foo", "bar");
    }

    @Test
    public void createAndGetAndListAndDeleteMessage() {
        BackendMessage message = new BackendMessage(MY_TYPE, new Date(), 3600, false, null, payload, null);
        String messageId = aiqService.createBackendMessage(message);

        EnrichedBackendMessage createdMessage = aiqService.fetchBackendMessage(messageId);

        assertEquals(message.getType(), createdMessage.getType());
        assertEquals(message.getActiveFrom().getTime() / 1000L, createdMessage.getActiveFrom().getTime() / 1000L);
        assertEquals(message.getTimeToLive(), createdMessage.getTimeToLive());
        assertEquals(message.isUrgent(), createdMessage.isUrgent());
        assertEquals(message.get_launchable(), createdMessage.get_launchable());
        assertEquals(message.getPayload(), createdMessage.getPayload());

        assertNotNull(createdMessage.get_id());
        assertNotNull(createdMessage.getCreated());
        assertEquals(messageId, createdMessage.get_id());

        List<EnrichedBackendMessage> messages1 = aiqService.fetchBackendMessages(MY_TYPE, true);
        assertTrue(messages1.size() > 1);
        assertNotNull(messages1.get(0).getPayload());

        List<EnrichedBackendMessage> messages2 = aiqService.fetchBackendMessages(MY_TYPE, false);
        assertTrue(messages2.size() > 1);
        assertNull(messages2.get(0).getPayload());

        List<EnrichedBackendMessage> messages3 = aiqService.fetchBackendMessages(true);
        assertTrue(messages3.size() > 1);
        assertNotNull(messages3.get(0).getPayload());

        List<EnrichedBackendMessage> messages4 = aiqService.fetchBackendMessages(false);
        assertTrue(messages4.size() > 1);
        assertNull(messages4.get(0).getPayload());

        List<EnrichedBackendMessage> messages5 = aiqService.fetchBackendMessages();
        assertTrue(messages5.size() > 1);
        assertNotNull(messages5.get(0).getPayload());

        assertTrue(aiqService.deleteBackendMessage(messageId));

        assertNull(aiqService.fetchBackendMessage(createdMessage.get_id()));

        assertFalse(aiqService.deleteBackendMessage(createdMessage.get_id()));
    }

    @Test
    public void createMessageWithNullActiveFrom() {
        BackendMessage message = new BackendMessage(MY_TYPE, null, 3600, false, null, payload, null);
        String messageId = aiqService.createBackendMessage(message);

        EnrichedBackendMessage createdMessage = aiqService.fetchBackendMessage(messageId);

        assertEquals(createdMessage.getCreated().getTime(), createdMessage.getActiveFrom().getTime());
    }

    @Test
    public void createMessageWithRecipients() {
        User user = aiqService.fetchUsers().get(0);

        BackendMessage message = new BackendMessage(MY_TYPE, null, 3600, false, null, payload,
                new BackendMessageRecipients(Arrays.asList(user.get_id()), null),
                null);
        String messageId = aiqService.createBackendMessage(message);

        EnrichedBackendMessage createdMessage = aiqService.fetchBackendMessage(messageId);

        assertEquals(createdMessage.getCreated().getTime(), createdMessage.getActiveFrom().getTime());
    }

    @Test
    public void createMessageWithAttachments() {
        BackendMessage message = new BackendMessage(MY_TYPE, null, 3600, false, null, payload, null, null);
        String messageId = aiqService.createBackendMessage(message, Arrays.asList(
                new MessageAttachment(PHOTO, MediaType.IMAGE_JPEG, photo),
                new MessageAttachment(null, MediaType.IMAGE_PNG, logo)
        ));

        EnrichedBackendMessage createdMessage = aiqService.fetchBackendMessage(messageId);

        assertEquals(createdMessage.getCreated().getTime(), createdMessage.getActiveFrom().getTime());
    }

    @Test
    public void createAndGetAndUpdateAndDeleteMessage() {
        BackendMessage message = new BackendMessage(MY_TYPE, new Date(), 3600, false, null, payload, null);
        String messageId = aiqService.createBackendMessage(message);

        EnrichedBackendMessage createdMessage = aiqService.fetchBackendMessage(messageId);

        payload.put("temp", "temp1");
        BackendMessageUpdate messageUpdate = new BackendMessageUpdate(7200, payload, null);
        assertTrue(aiqService.updateBackendMessage(createdMessage.get_id(), messageUpdate));

        EnrichedBackendMessage updatedMessage = aiqService.fetchBackendMessage(messageId);
        assertNotNull(updatedMessage);
        assertEquals(messageUpdate.getTimeToLive(), updatedMessage.getTimeToLive());
        assertNotNull(updatedMessage.getPayload().get("temp"));

        assertTrue(aiqService.deleteBackendMessage(messageId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateBackendMessageNullPayLoad(){
        new BackendMessage(MY_TYPE, new Date(), 3600, false, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateBackendMessageNullType(){
        new BackendMessage(null, new Date(), 3600, false, null, payload, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateBackendMessageEmptyType(){
        new BackendMessage("", new Date(), 3600, false, null, payload, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateBackendMessageInvalidTTL(){
        new BackendMessage(MY_TYPE, new Date(), -1, false, null, payload, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateBackendMessageNullNotificationMessage(){
        new BackendMessage(MY_TYPE, new Date(), 3600, false, null, payload,
                new BackendMessageNotification(false, false, null, null));
    }
}
