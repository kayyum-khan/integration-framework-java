package com.appearnetworks.aiq.integrationframework.impl;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
    private static final String PLATFORM_INTEGRATION_USER = "AIQ8Platform";
    private static final String REALM = "AIQ8IntegrationAdapter";

    @Autowired
    private ServerRegistrator serverRegistrator;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptorAdapter() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if (isAuthorized(request))
                    return super.preHandle(request, response, handler);
                else {
                    response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
            }
        }).addPathPatterns("/aiq/integration/**");
    }

    private boolean isAuthorized(HttpServletRequest request) {
        // no authentication if password is not set
        if (serverRegistrator.getPassword() == null)
            return true;

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            return false;
        }
        String encodedValue = authHeader.split(" ")[1];
        String decodedValue = new String(Base64.decodeBase64(encodedValue));
        String authPair = PLATFORM_INTEGRATION_USER + ":" + serverRegistrator.getPassword();
        return decodedValue.equals(authPair);
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}
