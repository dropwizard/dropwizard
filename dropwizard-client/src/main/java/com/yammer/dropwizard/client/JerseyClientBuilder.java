package com.yammer.dropwizard.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A convenience class for building {@link Client} instances.
 * <p>
 * Among other things,
 * <ul>
 *     <li>Backed by Apache HttpClient</li>
 *     <li>Disables stale connection checks</li>
 *     <li>Disables Nagle's algorithm</li>
 *     <li>Disables cookie management by default</li>
 * </ul>
 * @see HttpClientBuilder
 */
public class JerseyClientBuilder {
    private final HttpClientBuilder builder = new HttpClientBuilder();
    private final ApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();

    private JerseyClientConfiguration configuration = new JerseyClientConfiguration();

    private Environment environment;
    private ObjectMapper objectMapper;
    private ExecutorService executorService;

    /**
     * Adds the given object as a Jersey provider.
     *
     * @param provider a Jersey provider
     * @return {@code this}
     */
    public JerseyClientBuilder withProvider(Object provider) {
        config.getSingletons().add(checkNotNull(provider));
        return this;
    }

    /**
     * Adds the given class as a Jersey provider. <p/><b>N.B.:</b> This class must either have a
     * no-args constructor or use Jersey's built-in dependency injection.
     *
     * @param klass a Jersey provider class
     * @return {@code this}
     */
    public JerseyClientBuilder withProvider(Class<?> klass) {
        config.getClasses().add(checkNotNull(klass));
        return this;
    }

    /**
     * Sets the state of the given Jersey feature.
     *
     * @param featureName     the name of the Jersey feature
     * @param featureState    the state of the Jersey feature
     * @return {@code this}
     */
    public JerseyClientBuilder withFeature(String featureName, boolean featureState) {
        config.getFeatures().put(featureName, featureState);
        return this;
    }

    /**
     * Sets the state of the given Jersey property.
     *
     * @param propertyName     the name of the Jersey property
     * @param propertyValue    the state of the Jersey property
     * @return {@code this}
     */
    public JerseyClientBuilder withProperty(String propertyName, String propertyValue) {
        config.getProperties().put(propertyName, propertyValue);
        return this;
    }

    /**
     * Uses the given {@link JerseyClientConfiguration}.
     *
     * @param configuration    a configuration object
     * @return {@code this}
     */
    public JerseyClientBuilder using(JerseyClientConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Uses the given {@link Environment}.
     *
     * @param environment    a Dropwizard {@link Environment}
     * @return {@code this}
     * @see #using(java.util.concurrent.ExecutorService, com.fasterxml.jackson.databind.ObjectMapper)
     */
    public JerseyClientBuilder using(Environment environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Uses the given {@link ExecutorService} and {@link ObjectMapper}.
     *
     * @param executorService    a thread pool
     * @param objectMapper       an object mapper
     * @return {@code this}
     * @see #using(com.yammer.dropwizard.config.Environment)
     */
    public JerseyClientBuilder using(ExecutorService executorService, ObjectMapper objectMapper) {
        this.executorService = executorService;
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Builds the {@link Client} instance.
     *
     * @return a fully-configured {@link Client}
     */
    public Client build() {
        if ((environment == null) && (executorService == null) && (objectMapper == null)) {
            throw new IllegalStateException("Must have either an environment or both an executor service and an object mapper");
        }

        if (environment == null) {
            return build(executorService, objectMapper);
        }

        return build(environment.managedExecutorService("jersey-client-%d",
                                                        configuration.getMinThreads(),
                                                        configuration.getMaxThreads(),
                                                        60,
                                                        TimeUnit.SECONDS),
                     environment.getObjectMapperFactory().build());
    }

    private Client build(ExecutorService threadPool,
                         ObjectMapper objectMapper) {
        final ApacheHttpClient4Handler handler = new ApacheHttpClient4Handler(builder.build(),
                                                                              null,
                                                                              true);
        config.getSingletons().add(new JacksonMessageBodyProvider(objectMapper));

        final ApacheHttpClient4 client = new ApacheHttpClient4(handler, config);
        client.setExecutorService(threadPool);

        if (configuration.isGzipEnabled()) {
            client.addFilter(new GZIPContentEncodingFilter(configuration.isCompressRequestEntity()));
        }

        return client;
    }
}
