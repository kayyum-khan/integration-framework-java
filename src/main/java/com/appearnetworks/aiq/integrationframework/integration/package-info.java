/**
 * Communication from the server to the integration adapter.
 * <p>
 * The framework expect exactly one bean implementing {@link com.appearnetworks.aiq.integrationframework.integration.IntegrationAdapter}
 * in the Spring application context. This is done easiest my annotating your implementation class with
 * {@link org.springframework.stereotype.Component} and including it in Spring's component scanning.
 * <p>
 * You can either implement {@link com.appearnetworks.aiq.integrationframework.integration.IntegrationAdapter},
 * or extend {@link com.appearnetworks.aiq.integrationframework.integration.IntegrationAdapterBase} and only
 * implement the methods you need. The implementation needs to be thread-safe.
 *
 * @see com.appearnetworks.aiq.integrationframework.integration.IntegrationAdapter
 * @see com.appearnetworks.aiq.integrationframework.integration.IntegrationAdapterBase
 */
package com.appearnetworks.aiq.integrationframework.integration;