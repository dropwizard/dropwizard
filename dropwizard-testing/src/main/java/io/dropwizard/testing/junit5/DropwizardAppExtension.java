package io.dropwizard.testing.junit5;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.Command;
import io.dropwizard.core.cli.ServerCommand;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import jakarta.ws.rs.client.Client;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.jspecify.annotations.Nullable;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5HttpClientBuilderConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.RequestEntityProcessing;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

//@formatter:off
/**
 * An extension for starting and stopping your application at the start and end of a test class.
 * <p>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).
 * </p>
 *
 * @param <C> the configuration type
 */
//@formatter:on
public class DropwizardAppExtension<C extends Configuration> implements DropwizardExtension,
    BeforeAllCallback, AfterAllCallback {

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 1000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 5000;

    private final DropwizardTestSupport<C> testSupport;

    private final AtomicInteger recursiveCallCount = new AtomicInteger(0);

    @Nullable
    private Client client;

    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass) {
        this(applicationClass, (String) null);
    }

    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass,
                                  @Nullable String configPath,
                                  ConfigOverride... configOverrides) {
        this(applicationClass, configPath, (String) null, configOverrides);
    }

    /**
     * @since 2.0
     */
    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass,
                                  @Nullable String configPath,
                                  ConfigurationSourceProvider configSourceProvider,
                                  ConfigOverride... configOverrides) {
        this(applicationClass, configPath, configSourceProvider, null, configOverrides);
    }

    /**
     * @since 2.0
     */
    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass,
                                  @Nullable String configPath,
                                  @Nullable String customPropertyPrefix,
                                  ConfigOverride... configOverrides) {
        this(applicationClass, configPath, customPropertyPrefix, ServerCommand::new, configOverrides);
    }

    /**
     * @since 2.0
     */
    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass,
                                  @Nullable String configPath,
                                  ConfigurationSourceProvider configSourceProvider,
                                  @Nullable String customPropertyPrefix,
                                  ConfigOverride... configOverrides) {
        this(applicationClass, configPath, configSourceProvider, customPropertyPrefix, ServerCommand::new, configOverrides);
    }

    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass,
                                  @Nullable String configPath,
                                  @Nullable String customPropertyPrefix,
                                  Function<Application<C>, Command> commandInstantiator,
                                  ConfigOverride... configOverrides) {
        this(new DropwizardTestSupport<>(applicationClass, configPath, customPropertyPrefix, commandInstantiator, configOverrides));
    }

    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass,
                                  @Nullable String configPath,
                                  ConfigurationSourceProvider configSourceProvider,
                                  @Nullable String customPropertyPrefix,
                                  Function<Application<C>, Command> commandInstantiator,
                                  ConfigOverride... configOverrides) {
        this(new DropwizardTestSupport<>(applicationClass, configPath, configSourceProvider, customPropertyPrefix, commandInstantiator, configOverrides));
    }

    /**
     * Alternate constructor that allows specifying exact Configuration object to
     * use, instead of reading a resource and binding it as Configuration object.
     *
     * @since 0.9
     */
    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass,
                                  C configuration) {
        this(new DropwizardTestSupport<>(applicationClass, configuration));
    }

    /**
     * Alternate constructor that allows specifying the command the Dropwizard application is started with.
     *
     * @since 1.1.0
     */
    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass,
                                  C configuration, Function<Application<C>, Command> commandInstantiator) {
        this(new DropwizardTestSupport<>(applicationClass, configuration, commandInstantiator));
    }

    public DropwizardAppExtension(DropwizardTestSupport<C> testSupport) {
        this.testSupport = testSupport;
    }

    public DropwizardAppExtension<C> addListener(final ServiceListener<C> listener) {
        this.testSupport.addListener(new DropwizardTestSupport.ServiceListener<C>() {
            @Override
            public void onRun(C configuration, Environment environment, DropwizardTestSupport<C> rule) throws Exception {
                listener.onRun(configuration, environment, DropwizardAppExtension.this);
            }

            @Override
            public void onStop(DropwizardTestSupport<C> rule) throws Exception {
                listener.onStop(DropwizardAppExtension.this);
            }
        });
        return this;
    }

    public DropwizardAppExtension<C> manage(final Managed managed) {
        return addListener(new ServiceListener<C>() {
            @Override
            public void onRun(C configuration, Environment environment, DropwizardAppExtension<C> rule) throws Exception {
                environment.lifecycle().manage(managed);
            }
        });
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        this.before();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        this.after();
    }

    @Override
    public void before() throws Exception {
        if (recursiveCallCount.getAndIncrement() == 0) {
            testSupport.before();
        }
    }

    @Override
    public void after() {
        if (recursiveCallCount.decrementAndGet() == 0) {
            testSupport.after();
            synchronized (this) {
                if (client != null) {
                    client.close();
                    client = null;
                }
            }
        }
    }

    public C getConfiguration() {
        return testSupport.getConfiguration();
    }

    public int getLocalPort() {
        return testSupport.getLocalPort();
    }

    public int getPort(int connectorIndex) {
        return testSupport.getPort(connectorIndex);
    }

    public int getAdminPort() {
        return testSupport.getAdminPort();
    }

    public Application<C> newApplication() {
        return testSupport.newApplication();
    }

    @SuppressWarnings({"TypeParameterUnusedInFormals"})
    public <A extends Application<C>> A getApplication() {
        return testSupport.getApplication();
    }

    public Environment getEnvironment() {
        return testSupport.getEnvironment();
    }

    public ObjectMapper getObjectMapper() {
        return testSupport.getObjectMapper();
    }

    public abstract static class ServiceListener<T extends Configuration> {

        public void onRun(T configuration, Environment environment, DropwizardAppExtension<T> rule) throws Exception {
            // Default NOP
        }

        public void onStop(DropwizardAppExtension<T> rule) throws Exception {
            // Default NOP
        }
    }

    public DropwizardTestSupport<C> getTestSupport() {
        return testSupport;
    }

    /**
     * Returns a new HTTP Jersey {@link Client} for performing HTTP requests against the tested
     * Dropwizard server. The client can be reused across different tests and automatically
     * closed along with the server. The client can be augmented by overriding the
     * {@link #clientBuilder()} method.
     *
     * @return a new {@link Client} managed by the extension.
     */
    public Client client() {
        synchronized (this) {
            if (client == null) {
                client = clientBuilder().build();
            }
            return client;
        }
    }

    protected JerseyClientBuilder clientBuilder() {
        ClientConfig clientConfig = new ClientConfig();
        Apache5HttpClientBuilderConfigurator contentCompressionConfigurator =
            HttpClientBuilder::disableContentCompression;
        clientConfig.connectorProvider(new Apache5ConnectorProvider())
            .register(new JacksonFeature(getObjectMapper()))
            .register(contentCompressionConfigurator)
            .property(ClientProperties.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT_MS)
            .property(ClientProperties.READ_TIMEOUT, DEFAULT_READ_TIMEOUT_MS)
            .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
        return new JerseyClientBuilder().withConfig(clientConfig);
    }
}
