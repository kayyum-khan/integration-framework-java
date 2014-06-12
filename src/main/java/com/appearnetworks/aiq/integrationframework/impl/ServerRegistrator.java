package com.appearnetworks.aiq.integrationframework.impl;

import com.appearnetworks.aiq.integrationframework.impl.server.IntegrationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Register the IA with server on startup, and unregister on shutdown.
 */
@Component
public class ServerRegistrator {
    private String password;

    @Value("${aiq.integration.url}")
    private String integrationUrl;

    @Autowired
    private IntegrationServiceImpl integrationService;

    private SecureRandom random = new SecureRandom();

    @PostConstruct
    public void register() {
        if (!integrationUrl.isEmpty()) {
            password = generateRandomPassword();
            integrationService.register(integrationUrl, password);
        }
    }

    private String generateRandomPassword() {
        return new BigInteger(130, random).toString(32);
    }

    @PreDestroy
    public void unregister() {
        if (!integrationUrl.isEmpty()) {
            integrationService.unregister();
        }
        password = null;
    }

    public String getPassword() {
        return password;
    }
}
