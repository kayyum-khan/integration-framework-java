/**
 * Communication from the integration adapter to the server.
 *<p>
 * The entry point is {@link com.appearnetworks.aiq.integrationframework.server.IntegrationService}
 * and the framework will provide exactly one thread-safe implementation of it in the Spring application context.
 * The easiest way to obtain a reference to it is:
 * <pre>
 * {@literal @Autowired}
 * private IntegrationService integrationService;
 * </pre>
 *
 * @see com.appearnetworks.aiq.integrationframework.server.IntegrationService
 */
package com.appearnetworks.aiq.integrationframework.server;