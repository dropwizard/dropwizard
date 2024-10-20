package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient5.HttpClientMetricNameStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.gzip.ConfiguredGZipEncoder;
import io.dropwizard.jersey.gzip.GZipDecoder;
import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.jersey.validation.HibernateValidationBinder;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.lifecycle.Managed;
import jakarta.validation.Validator;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.RxInvokerProvider;
import jakarta.ws.rs.core.Configuration;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import javax.net.ssl.HostnameVerifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;


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

    private final List<Object> singletons = new ArrayList<>();
    private final List<Class<?>> providers = new ArrayList<>();
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private JerseyClientConfiguration configuration = new JerseyClientConfiguration();

    private HttpClientBuilder apacheHttpClientBuilder;
    private Validator validator = Validators.newValidator();

    @Nullable
    private Environment environment;

    @Nullable
    private ObjectMapper objectMapper;

    @Nullable
    private ExecutorService executorService;

    @Nullable
    private ConnectorProvider connectorProvider;

    public JerseyClientBuilder(Environment environment) {
        this.apacheHttpClientBuilder = new HttpClientBuilder(environment);
        this.environment = environment;
    }

    public JerseyClientBuilder(MetricRegistry metricRegistry) {
        this.apacheHttpClientBuilder = new HttpClientBuilder(metricRegistry);
    }

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
        singletons.add(requireNonNull(provider));
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
        providers.add(requireNonNull(klass));
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
     * @see #using(Environment)
     */
    public JerseyClientBuilder using(ExecutorService executorService, ObjectMapper objectMapper) {
        this.executorService = executorService;
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Uses the given {@link ExecutorService}.
     *
     * @param executorService a thread pool
     * @return {@code this}
     * @see #using(Environment)
     */
    public JerseyClientBuilder using(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /**
     * Uses the given {@link ObjectMapper}.
     *
     * @param objectMapper    an object mapper
     * @return {@code this}
     * @see #using(Environment)
     */
    public JerseyClientBuilder using(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Use the given {@link ConnectorProvider} instance.
     * <p/><b>WARNING:</b> Use it with caution. Most features will not work in
     * a custom connection provider.
     *
     * @param connectorProvider a {@link ConnectorProvider} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(ConnectorProvider connectorProvider) {
        this.connectorProvider = connectorProvider;
        return this;
    }

    /**
     * Uses the {@link org.apache.hc.client5.http.HttpRequestRetryStrategy} for handling request retries.
     *
     * @param httpRequestRetryStrategy a {@link HttpRequestRetryStrategy}
     * @return {@code this}
     */
    public JerseyClientBuilder using(HttpRequestRetryStrategy httpRequestRetryStrategy) {
        apacheHttpClientBuilder.using(httpRequestRetryStrategy);
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
     * Use the given {@link HostnameVerifier} instance.
     *
     * Note that if {@link io.dropwizard.client.ssl.TlsConfiguration#isVerifyHostname()}
     * returns false, all host name verification is bypassed, including
     * host name verification performed by a verifier specified
     * through this interface.
     *
     * @param verifier a {@link HostnameVerifier} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(HostnameVerifier verifier) {
        apacheHttpClientBuilder.using(verifier);
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
     * Use the given {@link org.apache.hc.client5.http.auth.CredentialsStore} instance.
     *
     * @param credentialsStore a {@link org.apache.hc.client5.http.auth.CredentialsStore} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(CredentialsStore credentialsStore) {
        apacheHttpClientBuilder.using(credentialsStore);
        return this;
    }

    /**
     * Builds the {@link Client} instance with a custom reactive client provider.
     *
     * @return a fully-configured {@link Client}
     */
    public <RX extends RxInvokerProvider<?>> Client buildRx(String name, Class<RX> invokerType) {
        return build(name).register(invokerType);
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

        if (executorService == null) {
            // Create an ExecutorService based on the provided
            // configuration. The DisposableExecutorService decorator
            // is used to ensure that the service is shut down if the
            // Jersey client disposes of it.
            executorService = requireNonNull(environment).lifecycle()
                .executorService("jersey-client-" + name + "-%d")
                .minThreads(configuration.getMinThreads())
                .maxThreads(configuration.getMaxThreads())
                .workQueue(new ArrayBlockingQueue<>(configuration.getWorkQueueSize()))
                .build();
        }

        if (objectMapper == null) {
            objectMapper = requireNonNull(environment).getObjectMapper();
        }

        if (environment != null) {
            validator = environment.getValidator();
        }

        return build(name, executorService, objectMapper, validator);
    }

    private Client build(String name, ExecutorService threadPool,
                         ObjectMapper objectMapper,
                         Validator validator) {
        if (!configuration.isGzipEnabled()) {
            apacheHttpClientBuilder.disableContentCompression(true);
        }

        final Client client = org.glassfish.jersey.client.JerseyClientBuilder.createClient(buildConfig(name, threadPool, objectMapper, validator));
        client.register(new JerseyIgnoreRequestUserAgentHeaderFilter());

        // Tie the client to server lifecycle
        if (environment != null) {
            environment.lifecycle().manage(new Managed() {
                @Override
                public void stop() {
                    client.close();
                }
            });
        }
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

        config.register(new JacksonFeature(objectMapper));
        config.register(new HibernateValidationBinder(validator));

        for (Map.Entry<String, Object> property : this.properties.entrySet()) {
            config.property(property.getKey(), property.getValue());
        }

        config.register(new DropwizardExecutorProvider(threadPool));

        if (connectorProvider == null) {
            final ConfiguredCloseableHttpClient apacheHttpClient =
                    apacheHttpClientBuilder.buildWithDefaultRequestConfiguration(name);
            config.connectorProvider((client, runtimeConfig) -> createDropwizardApacheConnector(apacheHttpClient));
        } else {
            config.connectorProvider(connectorProvider);
        }

        return config;
    }

    /**
     * Builds {@link DropwizardApacheConnector} based on the configured Apache HTTP client
     * as {@link ConfiguredCloseableHttpClient} and the chunked encoding configuration set by the user.
     */
    protected DropwizardApacheConnector createDropwizardApacheConnector(ConfiguredCloseableHttpClient configuredClient) {
        return new DropwizardApacheConnector(configuredClient.getClient(), configuredClient.getDefaultRequestConfig(),
                configuration.isChunkedEncodingEnabled());
    }
}
