package io.dropwizard.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Throwables.propagate;

/**
 * A test support class for starting and stopping your application at the start and end of a test class.
 * <p>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).
 * </p>
 *
 * @param <C> the configuration type
 */
public class DropwizardTestSupport<C extends Configuration> {
    protected final Class<? extends Application<C>> applicationClass;
    protected final String configPath;
    protected final Set<ConfigOverride> configOverrides;
    protected final Optional<String> customPropertyPrefix;

    /**
     * Flag that indicates whether instance was constructed with an explicit
     * Configuration object or not; handling of the two cases differ.
     * Needed because state of {@link #configuration} changes during lifecycle.
     */
    protected final boolean explicitConfig;

    protected C configuration;
    protected Application<C> application;
    protected Environment environment;
    protected Server jettyServer;
    protected List<ServiceListener<C>> listeners = new ArrayList<>();

    public DropwizardTestSupport(Class<? extends Application<C>> applicationClass,
                             @Nullable String configPath,
                             ConfigOverride... configOverrides) {
        this(applicationClass, configPath, Optional.empty(), configOverrides);
    }

    public DropwizardTestSupport(Class<? extends Application<C>> applicationClass, String configPath,
                                 Optional<String> customPropertyPrefix, ConfigOverride... configOverrides) {
        this.applicationClass = applicationClass;
        this.configPath = configPath;
        this.configOverrides = ImmutableSet.copyOf(firstNonNull(configOverrides, new ConfigOverride[0]));
        this.customPropertyPrefix = customPropertyPrefix;
        explicitConfig = false;
    }

    /**
     * Alternative constructor that may be used to directly provide Configuration
     * to use, instead of specifying resource path for locating data to create
     * Configuration.
     *
     * @since 0.9
     *
     * @param applicationClass Type of Application to create
     * @param configuration Pre-constructed configuration object caller provides; will not
     *   be manipulated in any way, no overriding
     */
    public DropwizardTestSupport(Class<? extends Application<C>> applicationClass,
            C configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Can not pass null configuration for explicitly configured instance");
        }
        this.applicationClass = applicationClass;
        configPath = "";
        configOverrides = ImmutableSet.of();
        customPropertyPrefix = Optional.empty();
        this.configuration = configuration;
        explicitConfig = true;
    }

    public DropwizardTestSupport<C> addListener(ServiceListener<C> listener) {
        this.listeners.add(listener);
        return this;
    }

    public DropwizardTestSupport<C> manage(final Managed managed) {
        return addListener(new ServiceListener<C>() {
            @Override
            public void onRun(C configuration, Environment environment, DropwizardTestSupport<C> rule) throws Exception {
                environment.lifecycle().manage(managed);
            }
        });
    }

    public void before() {
        applyConfigOverrides();
        startIfRequired();
    }

    public void after() {
        try {
            stopIfRequired();
        } finally {
            resetConfigOverrides();
        }
    }

    private void stopIfRequired() {
        if (jettyServer != null) {
            for (ServiceListener<C> listener : listeners) {
                try {
                    listener.onStop(this);
                } catch (Exception ignored) {
                }
            }
            try {
                jettyServer.stop();
            } catch (Exception e) {
                throw propagate(e);
            } finally {
                jettyServer = null;
            }
        }
    }

    private void applyConfigOverrides() {
        for (ConfigOverride configOverride : configOverrides) {
            configOverride.addToSystemProperties();
        }
    }

    private void resetConfigOverrides() {
        for (ConfigOverride configOverride : configOverrides) {
            configOverride.removeFromSystemProperties();
        }
    }

    private void startIfRequired() {
        if (jettyServer != null) {
            return;
        }

        try {
            application = newApplication();

            final Bootstrap<C> bootstrap = new Bootstrap<C>(application) {
                @Override
                public void run(C configuration, Environment environment) throws Exception {
                    environment.lifecycle().addServerLifecycleListener(server -> jettyServer = server);
                    DropwizardTestSupport.this.configuration = configuration;
                    DropwizardTestSupport.this.environment = environment;
                    super.run(configuration, environment);
                    for (ServiceListener<C> listener : listeners) {
                        try {
                            listener.onRun(configuration, environment, DropwizardTestSupport.this);
                        } catch (Exception ex) {
                            throw new RuntimeException("Error running app rule start listener", ex);
                        }
                    }
                }
            };
            if (explicitConfig) {
                bootstrap.setConfigurationFactoryFactory((klass, validator, objectMapper, propertyPrefix) ->
                    new POJOConfigurationFactory<>(configuration));
            } else if (customPropertyPrefix.isPresent()) {
                bootstrap.setConfigurationFactoryFactory((klass, validator, objectMapper, propertyPrefix) ->
                    new ConfigurationFactory<>(klass, validator, objectMapper, customPropertyPrefix.get()));
            }

            application.initialize(bootstrap);
            final ServerCommand<C> command = new ServerCommand<>(application);

            final ImmutableMap.Builder<String, Object> file = ImmutableMap.builder();
            if (!Strings.isNullOrEmpty(configPath)) {
                file.put("file", configPath);
            }
            final Namespace namespace = new Namespace(file.build());

            command.run(bootstrap, namespace);
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    public C getConfiguration() {
        return configuration;
    }

    public int getLocalPort() {
        return ((ServerConnector) jettyServer.getConnectors()[0]).getLocalPort();
    }

    public int getAdminPort() {
        final Connector[] connectors = jettyServer.getConnectors();
        return ((ServerConnector) connectors[connectors.length -1]).getLocalPort();
    }

    public int getPort(int connectorIndex) {
        return ((ServerConnector) jettyServer.getConnectors()[connectorIndex]).getLocalPort();
    }

    public Application<C> newApplication() {
        try {
            return applicationClass.newInstance();
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <A extends Application<C>> A getApplication() {
        return (A) application;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ObjectMapper getObjectMapper() {
        return getEnvironment().getObjectMapper();
    }

    public abstract static class ServiceListener<T extends Configuration> {
        public void onRun(T configuration, Environment environment, DropwizardTestSupport<T> rule) throws Exception {
            // Default NOP
        }

        public void onStop(DropwizardTestSupport<T> rule) throws Exception {
            // Default NOP
        }
    }
}
