package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dropwizard.jersey.gzip.ConfiguredGZipEncoder;
import io.dropwizard.jersey.gzip.GZipDecoder;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.setup.Environment;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * A convenience class for building {@link Client} instances.
 * <p>
 * Among other things,
 * <ul>
 * <li>Backed by Apache HttpClient</li>
 * <li>Disables stale connection checks</li>
 * <li>Disables Nagle's algorithm</li>
 * <li>Disables cookie management by default</li>
 * </ul>
 * </p>
 *
 * @see HttpClientBuilder
 */
public class JerseyClientBuilder extends ApacheClientBuilderBase<JerseyClientBuilder, JerseyClientConfiguration> {

    private final List<Object> singletons = Lists.newArrayList();
    private final List<Class<?>> providers = Lists.newArrayList();
    private final Map<String, Object> properties = Maps.newLinkedHashMap();

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private ObjectMapper objectMapper;
    private ExecutorService executorService;
    private ConnectorProvider connectorProvider = new ApacheConnectorProvider();

    public JerseyClientBuilder(Environment environment) {
        super(environment, new JerseyClientConfiguration());
    }

    public JerseyClientBuilder(MetricRegistry metricRegistry) {
        super(metricRegistry, new JerseyClientConfiguration());
    }

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
     * Use the given {@link Validator} instance.
     *
     * @param validator a {@link Validator} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(Validator validator) {
        this.validator = validator;
        return this;
    }

    /**
     * Uses the given {@link ExecutorService} and {@link ObjectMapper}.
     *
     * @param executorService a thread pool
     * @param objectMapper    an object mapper
     * @return {@code this}
     * @see #using(io.dropwizard.setup.Environment)
     */
    public JerseyClientBuilder using(ExecutorService executorService, ObjectMapper objectMapper) {
        this.executorService = executorService;
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Use the given {@link ConnectorProvider} instance.
     *
     * @param connectorProvider    a {@link ConnectorProvider} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(ConnectorProvider connectorProvider) {
        this.connectorProvider = connectorProvider;
        return this;
    }

    /**
     * Builds the {@link Client} instance.
     *
     * @return a fully-configured {@link Client}
     */
    public Client build(String name) {
        if ((environment == null) && ((executorService == null) || (objectMapper == null))) {
            throw new IllegalStateException("Must have either an environment or both " +
                    "an executor service and an object mapper");
        }

        if (environment == null) {
            return build(name, executorService, objectMapper, validator);
        }

        return build(name, environment.lifecycle()
                        .executorService("jersey-client-" + name + "-%d")
                        .minThreads(configuration.getMinThreads())
                        .maxThreads(configuration.getMaxThreads())
                        .build(),
                environment.getObjectMapper(),
                environment.getValidator());
    }

    private Client build(String name,
                         ExecutorService threadPool,
                         ObjectMapper objectMapper,
                         Validator validator) {

        final Client client = ClientBuilder.newClient(buildConfig(name, threadPool, objectMapper, validator));

        if (configuration.isGzipEnabled()) {
            client.register(new GZipDecoder());
            client.register(new ConfiguredGZipEncoder(configuration.isGzipEnabledForRequests()));
        }

        return client;
    }

    private Configuration buildConfig(final String name,
                                      final ExecutorService threadPool,
                                      final ObjectMapper objectMapper,
                                      final Validator validator) {


        final Integer timeout = (int) configuration.getTimeout().toMilliseconds();
        final Integer connectionTimeout = (int) configuration.getConnectionTimeout().toMilliseconds();

        final HttpClientConnectionManager connectionManager = createConnectionManager(registry, name);
        final ClientConfig config = new ClientConfig();
        final RequestConfig requestConfig = createRequestConfig();

        config.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
        config.property(ApacheClientProperties.DISABLE_COOKIES, !configuration.isCookiesEnabled());
        config.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
        config.property(ClientProperties.READ_TIMEOUT, timeout);
        config.property(ApacheClientProperties.REQUEST_CONFIG, requestConfig);

        if (credentialsProvider != null) {
            config.property(ApacheClientProperties.CREDENTIALS_PROVIDER, credentialsProvider);
        }

        for (Object singleton : this.singletons) {
            config.register(singleton);
        }

        for (Class<?> provider : this.providers) {
            config.register(provider);
        }

        config.register(new JacksonMessageBodyProvider(objectMapper, validator));

        for (Map.Entry<String, Object> property : this.properties.entrySet()) {
            config.property(property.getKey(), property.getValue());
        }

        final RequestEntityProcessing requestEntityProcessing = configuration.isChunkedEncodingEnabled() ? RequestEntityProcessing.CHUNKED : RequestEntityProcessing.BUFFERED;
        config.property(ClientProperties.REQUEST_ENTITY_PROCESSING, requestEntityProcessing);

        config.register(new DropwizardExecutorProvider(threadPool));
        config.connectorProvider(connectorProvider);

        return config;
    }

}
