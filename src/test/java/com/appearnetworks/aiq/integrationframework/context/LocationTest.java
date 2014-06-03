package com.appearnetworks.aiq.integrationframework.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LocationTest {
    @Test
    public void nullContextGivesNull() {
        assertNull(Location.fromContext(null));
    }

    @Test
    public void contextWithoutLocationGivesNull() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode clientContext = mapper.createObjectNode();
        clientContext.put("foo", mapper.createObjectNode());

        assertNull(Location.fromContext(clientContext));
    }

    @Test
    public void contextWithEmptyLocationGivesNull() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode clientContext = mapper.createObjectNode();
        clientContext.put("com.appearnetworks.aiq.location", mapper.createObjectNode());

        assertNull(Location.fromContext(clientContext));
    }

    @Test
    public void contextWithLocation() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode clientContext = mapper.createObjectNode();
        ObjectNode loc = mapper.createObjectNode();
        loc.put("latitude", 11.22);
        loc.put("longitude", -22.44);
        clientContext.put("com.appearnetworks.aiq.location", loc);

        Location location = Location.fromContext(clientContext);
        assertEquals(11.22, location.getLatitude(), 0.0);
        assertEquals(-22.44, location.getLongitude(), 0.0);
    }
}
