package com.appearnetworks.aiq.integrationframework.platform;

import com.appearnetworks.aiq.integrationframework.impl.platform.PlatformServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/META-INF/testApplicationContext.xml"})
public class PlatformTest {
    @Autowired
    private PlatformServiceImpl aiqService;

    @Test
    public void getClientSessions() {
        List<ClientSession> clientSessions = aiqService.fetchClientSessions();
        assertNotNull(clientSessions);
    }

    @Test
    public void getClientSessionNotFound() {
        ClientSession clientSession = aiqService.fetchClientSession("bogus");
        assertNull(clientSession);
    }

    @Test
    public void terminateClientSessionNotFound() {
        assertFalse(aiqService.terminateClientSession("bogus"));
    }

    @Test
    public void newDataAvailableForUsers() {
        aiqService.newDataAvailableForUsers(Arrays.asList("user1", "user2"));
    }

    @Test
    public void newDataAvailableForUsersUrgent() {
        aiqService.newDataAvailableForUsers(Arrays.asList("user1", "user2"), null);
    }

    @Test
    public void newDataAvailableForUsersWithCondition() {
        ObjectNode condition = new ObjectMapper().createObjectNode();
        condition.put("foo", "BAR");
        aiqService.newDataAvailableForUsers(Arrays.asList("user1", "user2"), condition);
    }

    @Test
    public void newDataAvailableForLaunchables() {
        aiqService.newDataAvailableForLaunchables(Arrays.asList("launchable1", "launchable2"));
    }

    @Test
    public void newDataAvailableForLaunchablesUrgent() {
        aiqService.newDataAvailableForLaunchables(Arrays.asList("launchable1", "launchable2"), null);
    }

    @Test
    public void newDataAvailableForLaunchablesWithCondition() {
        ObjectNode condition = new ObjectMapper().createObjectNode();
        condition.put("foo", "BAR");
        aiqService.newDataAvailableForLaunchables(Arrays.asList("launchable1", "launchable2"), condition);
    }

    @Test
    public void newDataAvailableForAllUsers() {
        aiqService.newDataAvailableForAllUsers();
    }

    @Test
    public void newDataAvailableForAllUsersUrgent() {
        aiqService.newDataAvailableForAllUsers(null);
    }

    @Test
    public void newDataAvailableForAllUsersWithCondition() {
        ObjectNode condition = new ObjectMapper().createObjectNode();
        condition.put("foo", "BAR");
        aiqService.newDataAvailableForAllUsers(condition);
    }

    @Test
    public void updateBackendContextForNonExistingSession() {
        assertFalse(aiqService.updateBackendContext("bogus", "bogus", "com.example.context", new ObjectMapper().createObjectNode()));
    }

    @Test
    public void removeBackendContextForNonExistingSession() {
        assertFalse(aiqService.removeBackendContext("bogus", "bogus", "com.example.context"));
    }

    @Test
    public void removeBackendContextForIllegalProvider() {
        try {
            aiqService.removeBackendContext("bogus", "bogus", "com.appearnetworks.aiq.bogus");
            fail("Should throw AIQInternalServerError");
        } catch (PlatformException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        }
    }

    @Test
    public void fetchUsers() {
        List<PlatformUser> users = aiqService.fetchUsers();
        assertNotNull(users);
        for (PlatformUser user : users) {
            List<String> roles = user.getRoles();
            assertNotNull(roles);
        }
    }

    @Test
    public void fetchUserNotFound() {
        PlatformUser user = aiqService.fetchUser("bogus");
        assertNull(user);
    }

    @Test
    public void registerAndUnregister() {
        aiqService.register("http://foo.bar/", "secret");
        aiqService.unregister();
    }
}
