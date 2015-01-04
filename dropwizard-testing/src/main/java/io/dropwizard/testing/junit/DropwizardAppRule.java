package io.dropwizard.testing.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.rules.ExternalResource;

import javax.annotation.Nullable;
import java.util.Enumeration;
import java.util.List;

import static com.google.common.base.Throwables.propagate;

/**
 * A JUnit rule for starting and stopping your application at the start and end of a test class.
 * <p>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).
 * </p>
 *
 * @param <C> the configuration type
 */
public class DropwizardAppRule<C extends Configuration> extends ExternalResource {

    private final Class<? extends Application<C>> applicationClass;
    private final String configPath;

    public static class ServiceListener<T extends Configuration> {
        public void onRun(T configuration, Environment environment, DropwizardAppRule<T> rule) throws Exception { }
        public void onStop(DropwizardAppRule<T> rule) throws Exception { }
    }

    private C configuration;
    private Application<C> application;
    private Environment environment;
    private Server jettyServer;
    private List<ServiceListener> listeners = Lists.newArrayList();

    public DropwizardAppRule(Class<? extends Application<C>> applicationClass,
                             @Nullable String configPath,
                             ConfigOverride... configOverrides) {
        this.applicationClass = applicationClass;
        this.configPath = configPath;
        for (ConfigOverride configOverride : configOverrides) {
            configOverride.addToSystemProperties();
        }
    }

    public DropwizardAppRule<C> addListener(ServiceListener<C> listener) {
        this.listeners.add(listener);
        return this;
    }

    public DropwizardAppRule<C> manage(final Managed managed) {
        return addListener(new ServiceListener<C>() {
            @Override
            public void onRun(C configuration, Environment environment, DropwizardAppRule<C> rule) throws Exception {
                environment.lifecycle().manage(managed);
            }
        });
    }

    @Override
    protected void before() {
        startIfRequired();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void after() {
        for (ServiceListener listener : listeners) {
            try { listener.onStop(this); } catch(Exception ignored) { }
        }
        resetConfigOverrides();
        try {
            jettyServer.stop();
        } catch (Exception e) {
            propagate(e);
        } finally {
            jettyServer = null;
        }
    }

    private void resetConfigOverrides() {
        for (Enumeration<?> props = System.getProperties().propertyNames(); props.hasMoreElements(); ) {
            String keyString = (String) props.nextElement();
            if (keyString.startsWith("dw.")) {
                System.clearProperty(keyString);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void startIfRequired() {
        if (jettyServer != null) {
            return;
        }

        try {
            application = newApplication();

            final Bootstrap<C> bootstrap = new Bootstrap<C>(application) {
                @Override
                public void run(C configuration, Environment environment) throws Exception {
                    environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
                        @Override
                        public void serverStarted(Server server) {
                            jettyServer = server;
                        }
                    });
                    DropwizardAppRule.this.configuration = configuration;
                    DropwizardAppRule.this.environment = environment;
                    super.run(configuration, environment);
                    for (ServiceListener listener : listeners) {
                        try {
                            listener.onRun(configuration, environment, DropwizardAppRule.this);
                        } catch(Exception ex) {
                            throw new RuntimeException("Error running app rule start listener", ex);
                        }
                    }
                }
            };

            application.initialize(bootstrap);
            final ServerCommand<C> command = new ServerCommand<>(application);

            ImmutableMap.Builder<String, Object> file = ImmutableMap.builder();
            if (!Strings.isNullOrEmpty(configPath)) {
                file.put("file", configPath);
            }
            final Namespace namespace = new Namespace(file.build());

            command.run(bootstrap, namespace);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public C getConfiguration() {
        return configuration;
    }

    public int getLocalPort() {
        return ((ServerConnector) jettyServer.getConnectors()[0]).getLocalPort();
    }

    public int getAdminPort() {
        return ((ServerConnector) jettyServer.getConnectors()[1]).getLocalPort();
    }

    public Application<C> newApplication() {
        try {
            return applicationClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
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

}
