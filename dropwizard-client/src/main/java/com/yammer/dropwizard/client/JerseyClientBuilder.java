package com.yammer.dropwizard.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.SchemeRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A convenience class for building {@link Client} instances.
 * <p/>
 * Among other things,
 *
 * <ul>
 *     <li>Backed by Apache HttpClient</li>
 *     <li>Disables stale connection checks</li>
 *     <li>Disables Nagle's algorithm</li>
 *     <li>Disables cookie management by default</li>
 * </ul>
 *
 * @see HttpClientBuilder
 */
public class JerseyClientBuilder {
    private final HttpClientBuilder builder = new HttpClientBuilder();
    private final List<Object> singletons = Lists.newArrayList();
    private final List<Class<?>> providers = Lists.newArrayList();
    private final Map<String, Boolean> features = Maps.newLinkedHashMap();
    private final Map<String, Object> properties = Maps.newLinkedHashMap();

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
        singletons.add(checkNotNull(provider));
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
        providers.add(checkNotNull(klass));
        return this;
    }

    /**
     * Sets the state of the given Jersey feature.
     *
     * @param featureName  the name of the Jersey feature
     * @param featureState the state of the Jersey feature
     * @return {@code this}
     */
    @SuppressWarnings("UnusedDeclaration") // basically impossible to test
    public JerseyClientBuilder withFeature(String featureName, boolean featureState) {
        features.put(featureName, featureState);
        return this;
    }

    /**
     * Sets the state of the given Jersey property.
     *
     * @param propertyName  the name of the Jersey property
     * @param propertyValue the state of the Jersey property
     * @return {@code this}
     */
    public JerseyClientBuilder withProperty(String propertyName, Object propertyValue) {
        properties.put(propertyName, propertyValue);
        return this;
    }

    /**
     * Uses the given {@link JerseyClientConfiguration}.
     *
     * @param configuration a configuration object
     * @return {@code this}
     */
    public JerseyClientBuilder using(JerseyClientConfiguration configuration) {
        this.configuration = configuration;
        builder.using(configuration);
        return this;
    }

    /**
     * Uses the given {@link Environment}.
     *
     * @param environment a Dropwizard {@link Environment}
     * @return {@code this}
     * @see #using(java.util.concurrent.ExecutorService, com.fasterxml.jackson.databind.ObjectMapper)
     */
    public JerseyClientBuilder using(Environment environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Use the given {@link DnsResolver} instance.
     *
     * @param resolver a {@link DnsResolver} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(DnsResolver resolver) {
        builder.using(resolver);
        return this;
    }

    /**
     * Use the given {@link SchemeRegistry} instance.
     *
     * @param registry a {@link SchemeRegistry} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(SchemeRegistry registry) {
        builder.using(registry);
        return this;
    }

    /**
     * Uses the given {@link ExecutorService} and {@link ObjectMapper}.
     *
     * @param executorService a thread pool
     * @param objectMapper    an object mapper
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
            throw new IllegalStateException("Must have either an environment or both " +
                                                    "an executor service and an object mapper");
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
        final Client client = new ApacheHttpClient4(buildHandler(), buildConfig(objectMapper));
        client.setExecutorService(threadPool);

        if (configuration.isGzipEnabled()) {
            client.addFilter(new GZIPContentEncodingFilter(configuration.isGzipEnabledForRequests()));
        }

        return client;
    }

    private ApacheHttpClient4Handler buildHandler() {
        return new ApacheHttpClient4Handler(builder.build(), null, true);
    }

    private ApacheHttpClient4Config buildConfig(ObjectMapper objectMapper) {
        final ApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getSingletons().addAll(singletons);
        config.getSingletons().add(new JacksonMessageBodyProvider(objectMapper));
        config.getClasses().addAll(providers);
        config.getFeatures().putAll(features);
        config.getProperties().putAll(properties);
        return config;
    }
}
