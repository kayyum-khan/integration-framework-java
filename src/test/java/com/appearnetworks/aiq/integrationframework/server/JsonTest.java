package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

public class JsonTest {
    private static final String URL = "http://example.com/path";

    private final ObjectMapper mapper = new ObjectMapper();

    private final User user = new User("userid", "username", "email", "fullName", Collections.singletonMap("key", "value"), Collections.singletonList("role"));

    @Test
    public void user() throws JsonProcessingException {
        String json = mapper.writeValueAsString(user);
        assertJsonEquals(
                "{\"_id\":\"userid\",\"username\":\"username\",\"email\":\"email\",\"fullName\":\"fullName\",\"profile\":{\"key\":\"value\"},\"roles\":[\"role\"]}",
                json);
    }

    @Test
    public void enrichedBackendMessage() throws JsonProcessingException {
        EnrichedBackendMessage message = new EnrichedBackendMessage("type", new Date(100), 17, true, null, mapper.createObjectNode(),
                new BackendMessageNotification(true, false, "message", null), "id", 1, new EnrichedBackendMessageLinks(URL),
                Collections.singletonList(new BackendMessageReadReport(user, 1, 2)));

        String json = mapper.writeValueAsString(message);
        assertJsonEquals(
                "{\"type\":\"type\",\"activeFrom\":100,\"timeToLive\":17,\"urgent\":true,\"_launchable\":null,\"payload\":{},\"notification\":{\"sound\":true,\"vibration\":false,\"message\":\"message\",\"condition\":null},\"_id\":\"id\",\"created\":1,\"links\":{\"self\":\"http://example.com/path\"},\"readBy\":[{\"user\":{\"_id\":\"userid\",\"username\":\"username\",\"email\":\"email\",\"fullName\":\"fullName\",\"profile\":{\"key\":\"value\"},\"roles\":[\"role\"]},\"revision\":1,\"readTimestamp\":2}]}",
                json);
    }

    @Test
    public void clientSession() throws JsonProcessingException {
        ClientSession clientSession = new ClientSession(user, "deviceId", new Date(1), new Date(2), mapper.createObjectNode(), "id", 17,
                new ClientSessionLinks(URL));

        String json = mapper.writeValueAsString(clientSession);
        assertJsonEquals(
                "{\"user\":{\"_id\":\"userid\",\"username\":\"username\",\"email\":\"email\",\"fullName\":\"fullName\",\"profile\":{\"key\":\"value\"},\"roles\":[\"role\"]},\"deviceId\":\"deviceId\",\"created\":1,\"lastAccessed\":2,\"context\":{},\"_id\":\"id\",\"_rev\":17,\"links\":{\"self\":\"http://example.com/path\"}}",
                json);
    }
}
