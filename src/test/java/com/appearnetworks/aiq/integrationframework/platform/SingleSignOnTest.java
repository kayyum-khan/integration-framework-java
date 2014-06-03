package com.appearnetworks.aiq.integrationframework.platform;

import com.appearnetworks.aiq.integrationframework.impl.platform.PlatformServiceImpl;
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
    private PlatformServiceImpl aiqService;

    @Test
    public void invalidTempToken() {
        assertNull(aiqService.validatePlatformUserToken("123-111"));
    }

    @Test
    public void validUserToken(){
        String userToken = aiqService.fetchPlatformUserToken();
        assertNotNull(userToken);

        PlatformUser authorizedUser = aiqService.validatePlatformUserToken(userToken);
        assertNotNull(authorizedUser);
        assertNotNull(authorizedUser.get_id());
    }
}
