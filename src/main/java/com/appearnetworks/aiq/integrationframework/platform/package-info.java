/**
 * Communication from the integration adapter to the platform.
 *<p>
 * The entry point is {@link com.appearnetworks.aiq.integrationframework.platform.PlatformService}
 * and the framework will provide exactly one thread-safe implementation of it in the Spring application context.
 * The easiest way to obtain a reference to it is:
 * <pre>
 * {@literal @Autowired}
 * private PlatformService platformService;
 * </pre>
 *
 * @see com.appearnetworks.aiq.integrationframework.platform.PlatformService
 */
package com.appearnetworks.aiq.integrationframework.platform;