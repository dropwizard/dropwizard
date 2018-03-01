package io.dropwizard.testing.junit5;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.jersey.jackson.JacksonBinder;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

//@formatter:off
/**
 * An extension for starting and stopping your application at the start and end of a test class.
 * <p>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).
 * </p>
 *
 * <p>
 * Using DropwizardAppExtension at the suite level can speed up test runs, as the application is only started and stopped
 * once for the entire suite:
 * </p>
 *
 * <pre>
 * public class MySuite {
 *   public static final DropwizardAppExtension&lt;MyConfig> DROPWIZARD = new DropwizardAppExtension&lt;>(...);
 * }
 * </pre>
 *
 * <p>
 * If the same instance of DropwizardAppExtension is reused at the suite- and class-level, then the application will be
 * started and stopped once, regardless of whether the entire suite or a single test is executed.
 * </p>
 *
 * <pre>
 * &#064;ExtendWith(DropwizardExtensionsSupport.class)
 * public class FooTest {
 *   public static final DropwizardAppExtension&lt;MyConfig> DROPWIZARD = MySuite.DROPWIZARD;
 *
 *   public void testFoo() { ... }
 * }
 *
 * &#064;ExtendWith(DropwizardExtensionsSupport.class)
 * public class BarTest {
 *   public static final DropwizardAppExtension&lt;MyConfig> DROPWIZARD = MySuite.DROPWIZARD;
 *
 *   public void testBar() { ... }
 * }
 * </pre>
 *
 * <p>
 *
 * </p>
 *
 * @param <C> the configuration type
 */
//@formatter:on
public class DropwizardAppExtension<C extends Configuration> implements DropwizardExtension {

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
        this(applicationClass, configPath, Optional.empty(), configOverrides);
    }

    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass, @Nullable String configPath,
                                  Optional<String> customPropertyPrefix, ConfigOverride... configOverrides) {
        this(applicationClass, configPath, customPropertyPrefix, ServerCommand::new, configOverrides);
    }

    public DropwizardAppExtension(Class<? extends Application<C>> applicationClass, @Nullable String configPath,
                                  Optional<String> customPropertyPrefix, Function<Application<C>,
        Command> commandInstantiator, ConfigOverride... configOverrides) {
        this(new DropwizardTestSupport<>(applicationClass, configPath, customPropertyPrefix, commandInstantiator,
            configOverrides));
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
    public void before() {
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

    @SuppressWarnings("unchecked")
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
        return new JerseyClientBuilder()
            .register(new JacksonBinder(getObjectMapper()))
            .property(ClientProperties.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT_MS)
            .property(ClientProperties.READ_TIMEOUT, DEFAULT_READ_TIMEOUT_MS)
            .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
    }
}
