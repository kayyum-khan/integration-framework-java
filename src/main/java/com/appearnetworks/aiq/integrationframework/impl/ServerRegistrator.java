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

    @Value("${aiq.integration.password:}")
    private String integrationPassword;

    @Autowired
    private IntegrationServiceImpl integrationService;

    private final SecureRandom random = new SecureRandom();

    @PostConstruct
    public void postConstruct() {
        password = integrationPassword.isEmpty() ? generateRandomPassword() : integrationPassword;
        if (!integrationUrl.isEmpty()) {
            register(integrationUrl, password);
        }
    }

    public void register(String url, String password) {
        integrationService.register(url, password);
    }

    public String generateRandomPassword() {
        return new BigInteger(130, random).toString(32);
    }

    @PreDestroy
    public void preDestroy() {
        if (!integrationUrl.isEmpty()) {
            unregister();
        } else {
            password = null;
        }
    }

    public void unregister() {
        integrationService.unregister();
        password = null;
    }

    public String getPassword() {
        return password;
    }
}
