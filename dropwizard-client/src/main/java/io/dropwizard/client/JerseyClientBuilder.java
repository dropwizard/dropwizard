package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dropwizard.jersey.gzip.ConfiguredGZipEncoder;
import io.dropwizard.jersey.gzip.GZipDecoder;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.setup.Environment;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.config.Registry;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
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
 * <li>Compress requests and decompress responses using GZIP</li>
 * <li>Supports parsing and generating JSON data using Jackson</li>
 * </ul>
 * </p>
 *
 * @see HttpClientBuilder
 */
public class JerseyClientBuilder {

    private final List<Object> singletons = Lists.newArrayList();
    private final List<Class<?>> providers = Lists.newArrayList();
    private final Map<String, Object> properties = Maps.newLinkedHashMap();
    private JerseyClientConfiguration configuration = new JerseyClientConfiguration();

    private HttpClientBuilder apacheHttpClientBuilder;
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private Environment environment;
    private ObjectMapper objectMapper;
    private ExecutorService executorService;
    private ConnectorProvider connectorProvider;

    public JerseyClientBuilder(Environment environment) {
        this.apacheHttpClientBuilder = new HttpClientBuilder(environment);
        this.environment = environment;
    }

    public JerseyClientBuilder(MetricRegistry metricRegistry) {
        this.apacheHttpClientBuilder = new HttpClientBuilder(metricRegistry);
    }

    @VisibleForTesting
    public void setApacheHttpClientBuilder(HttpClientBuilder apacheHttpClientBuilder) {
        this.apacheHttpClientBuilder = apacheHttpClientBuilder;
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
     * <p/>
     * <p/><b>WARNING:</b> The default connector ignores Jersey properties.
     * Use {@link JerseyClientConfiguration} instead.
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
        apacheHttpClientBuilder.using(configuration);
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
     * <p/><b>WARNING:</b> Use it with a caution. Most of features will not
     * work in a custom connection provider.
     *
     * @param connectorProvider a {@link ConnectorProvider} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(ConnectorProvider connectorProvider) {
        this.connectorProvider = connectorProvider;
        return this;
    }

    /**
     * Uses the {@link org.apache.http.client.HttpRequestRetryHandler} for handling request retries.
     *
     * @param httpRequestRetryHandler a HttpRequestRetryHandler
     * @return {@code this}
     */
    public JerseyClientBuilder using(HttpRequestRetryHandler httpRequestRetryHandler) {
        apacheHttpClientBuilder.using(httpRequestRetryHandler);
        return this;
    }

    /**
     * Use the given {@link DnsResolver} instance.
     *
     * @param resolver a {@link DnsResolver} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(DnsResolver resolver) {
        apacheHttpClientBuilder.using(resolver);
        return this;
    }

    /**
     * Use the given {@link Registry} instance of connection socket factories.
     *
     * @param registry a {@link Registry} instance of connection socket factories
     * @return {@code this}
     */
    public JerseyClientBuilder using(Registry<ConnectionSocketFactory> registry) {
        apacheHttpClientBuilder.using(registry);
        return this;
    }

    /**
     * Use the given {@link HttpClientMetricNameStrategy} instance.
     *
     * @param metricNameStrategy a {@link HttpClientMetricNameStrategy} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(HttpClientMetricNameStrategy metricNameStrategy) {
        apacheHttpClientBuilder.using(metricNameStrategy);
        return this;
    }

    /**
     * Use the given environment name. This is used in the user agent.
     *
     * @param environmentName an environment name to use in the user agent.
     * @return {@code this}
     */
    public JerseyClientBuilder name(String environmentName) {
        apacheHttpClientBuilder.name(environmentName);
        return this;
    }

    /**
     * Use the given {@link HttpRoutePlanner} instance.
     *
     * @param routePlanner a {@link HttpRoutePlanner} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(HttpRoutePlanner routePlanner) {
        apacheHttpClientBuilder.using(routePlanner);
        return this;
    }

    /**
     * Use the given {@link CredentialsProvider} instance.
     *
     * @param credentialsProvider a {@link CredentialsProvider} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(CredentialsProvider credentialsProvider) {
        apacheHttpClientBuilder.using(credentialsProvider);
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
                        .workQueue(new ArrayBlockingQueue<Runnable>(configuration.getWorkQueueSize()))
                        .build(),
                environment.getObjectMapper(),
                environment.getValidator()
        );
    }

    private Client build(String name, ExecutorService threadPool,
                         ObjectMapper objectMapper,
                         Validator validator) {
        final Client client = ClientBuilder.newClient(buildConfig(name, threadPool, objectMapper, validator));

        if (configuration.isGzipEnabled()) {
            client.register(new GZipDecoder());
            client.register(new ConfiguredGZipEncoder(configuration.isGzipEnabledForRequests()));
        }

        return client;
    }

    private Configuration buildConfig(final String name, final ExecutorService threadPool,
                                      final ObjectMapper objectMapper,
                                      final Validator validator) {
        final ClientConfig config = new ClientConfig();

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

        config.register(new DropwizardExecutorProvider(threadPool));
        if (connectorProvider == null) {
            final ConfiguredCloseableHttpClient apacheHttpClient =
                    apacheHttpClientBuilder.buildWithDefaultRequestConfiguration(name);
            connectorProvider = new ConnectorProvider() {
                @Override
                public Connector getConnector(Client client, Configuration runtimeConfig) {
                    return new DropwizardApacheConnector(
                            apacheHttpClient.getClient(),
                            apacheHttpClient.getDefaultRequestConfig(),
                            configuration.isChunkedEncodingEnabled());
                }
            };
        }
        config.connectorProvider(connectorProvider);

        return config;
    }
}
