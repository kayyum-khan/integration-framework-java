package com.appearnetworks.aiq.integrationframework.server;

import com.appearnetworks.aiq.integrationframework.impl.server.IntegrationServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/META-INF/testApplicationContext.xml"})
public class SingleSignOnTest {

    @Autowired
    private IntegrationServiceImpl aiqService;

    @Test
    public void invalidTempToken() {
        assertNull(aiqService.validateUserToken("123-111"));
    }

    @Test
    public void validUserToken(){
        String userToken = aiqService.fetchUserToken();
        assertNotNull(userToken);

        User authorizedUser = aiqService.validateUserToken(userToken);
        assertNotNull(authorizedUser);
        assertNotNull(authorizedUser.get_id());
    }
}
