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
import java.util.UUID;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/META-INF/testApplicationContext.xml"})
public class BackendMessageTest {
    private static final String PHOTO = "photo";
    private static byte[] photo = new byte[]{0, 1, 2, 6, 76};
    private static byte[] logo = new byte[]{11, 56, 45, 43, 17};

    private ObjectMapper mapper = new ObjectMapper();

    private ObjectNode payload;

    @Autowired
    private IntegrationService aiqService;

    @Before
    public void setUp() {
        payload = mapper.createObjectNode();
        payload.put("foo", "bar");
    }

    @Test
    public void getAllMessages() {
       assertNotNull("Service returned null for get all messages", aiqService.fetchBackendMessages());
    }

    @Test
    public void createAndGetAndDeleteMessage() {
        BackendMessage message = new BackendMessage("MyType", new Date(), 3600, false, null, payload, null);
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

        assertTrue(aiqService.deleteBackendMessage(messageId));

        assertNull(aiqService.fetchBackendMessage(createdMessage.get_id()));

        assertFalse(aiqService.deleteBackendMessage(createdMessage.get_id()));
    }

    @Test
    public void createMessageWithNullActiveFrom() {
        BackendMessage message = new BackendMessage("MyType", null, 3600, false, null, payload, null);
        String messageId = aiqService.createBackendMessage(message);

        EnrichedBackendMessage createdMessage = aiqService.fetchBackendMessage(messageId);

        assertEquals(createdMessage.getCreated().getTime(), createdMessage.getActiveFrom().getTime());
    }

    @Test
    public void createMessageWithRecipients() {
        User user = aiqService.fetchUsers().get(0);

        BackendMessage message = new BackendMessage("MyType", null, 3600, false, null, payload,
                new BackendMessageRecipients(Arrays.asList(user.get_id()), null),
                null);
        String messageId = aiqService.createBackendMessage(message);

        EnrichedBackendMessage createdMessage = aiqService.fetchBackendMessage(messageId);

        assertEquals(createdMessage.getCreated().getTime(), createdMessage.getActiveFrom().getTime());
    }

    @Test
    public void createMessageWithAttachments() {
        BackendMessage message = new BackendMessage("MyType", null, 3600, false, null, payload, null, null);
        String messageId = aiqService.createBackendMessage(message, Arrays.asList(
                new MessageAttachment(PHOTO, MediaType.IMAGE_JPEG, photo),
                new MessageAttachment(null, MediaType.IMAGE_PNG, logo)
        ));

        EnrichedBackendMessage createdMessage = aiqService.fetchBackendMessage(messageId);

        assertEquals(createdMessage.getCreated().getTime(), createdMessage.getActiveFrom().getTime());
    }

    @Test
    public void createAndGetAndUpdateAndDeleteMessage() {
        BackendMessage message = new BackendMessage("MyType", new Date(), 3600, false, null, payload, null);
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
        new BackendMessage("MyType", new Date(), 3600, false, null, null, null);
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
        new BackendMessage("MyType", new Date(), -1, false, null, payload, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateBackendMessageNullNotificationMessage(){
        new BackendMessage("MyType", new Date(), 3600, false, null, payload,
                new BackendMessageNotification(false, false, null, null));
    }

    @Test
    public void distributionLists() {
        User user = aiqService.fetchUsers().get(0);

        String dl1Id = aiqService.createDistributionList(Arrays.asList(user.get_id()));
        String dl2Id = UUID.randomUUID().toString();
        aiqService.createDistributionList(dl2Id, Collections.<String>emptyList());

        boolean dl1Found = false;
        boolean dl2Found = false;
        for (DistributionList dl : aiqService.fetchDistributionLists()) {
            if (dl.get_id().equals(dl1Id)) dl1Found = true;
            if (dl.get_id().equals(dl2Id)) dl2Found = true;
        }
        assertTrue(dl1Found);
        assertTrue(dl2Found);

        DistributionList dl1 = aiqService.fetchDistributionList(dl1Id);
        assertEquals(dl1Id, dl1.get_id());
        assertEquals(1, dl1.getUsers().size());
        DistributionList dl2 = aiqService.fetchDistributionList(dl2Id);
        assertEquals(dl2Id, dl2.get_id());
        assertEquals(0, dl2.getUsers().size());

        BackendMessage message = new BackendMessage("MyType", new Date(), 3600, false, null, payload,
                new BackendMessageRecipients(null, Arrays.asList(dl1Id, dl2Id)), null);
        String messageId = aiqService.createBackendMessage(message);
        assertNotNull(messageId);

        assertTrue(aiqService.updateDistributionList(dl2Id, Arrays.asList(user.get_id())));
        dl2 = aiqService.fetchDistributionList(dl2Id);
        assertEquals(1, dl2.getUsers().size());

        aiqService.deleteBackendMessage(messageId);

        assertTrue(aiqService.deleteDistributionList(dl1Id));
        assertTrue(aiqService.deleteDistributionList(dl2Id));

        assertNull(aiqService.fetchDistributionList(dl1Id));
        assertNull(aiqService.fetchDistributionList(dl2Id));
    }

}
