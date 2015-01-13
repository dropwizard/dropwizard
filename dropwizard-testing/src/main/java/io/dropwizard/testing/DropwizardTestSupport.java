package io.dropwizard.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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

import javax.annotation.Nullable;
import java.util.List;
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
public class DropwizardTestSupport<C extends Configuration> extends CommandHelper<C> {

    private C configuration;
    private Environment environment;
    private Server jettyServer;
    private List<ServiceListener> listeners = Lists.newArrayList();

    public DropwizardTestSupport(Class<? extends Application<C>> applicationClass,
                             @Nullable String configPath,
                             ConfigOverride... configOverrides) {
        super(applicationClass, configPath, configOverrides);
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

    @SuppressWarnings("unchecked")
    public void after() {
        for (ServiceListener listener : listeners) {
            try {
                listener.onStop(this);
            } catch (Exception ignored) {
            }
        }
        resetConfigOverrides();
        try {
            jettyServer.stop();
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            jettyServer = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void startIfRequired() {
        if (jettyServer != null) {
            return;
        }

        try {
            initialize();

            final ServerCommand<C> command = new ServerCommand<>(application);
            command.run(bootstrap, namespace);
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    @Override
    public Bootstrap<C> newBootStrap(Application<C> app) {
        return new Bootstrap<C>(app) {
            @Override
            public void run(C configuration, Environment environment) throws Exception {
                environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
                    @Override
                    public void serverStarted(Server server) {
                        jettyServer = server;
                    }
                });
                DropwizardTestSupport.this.configuration = configuration;
                DropwizardTestSupport.this.environment = environment;
                super.run(configuration, environment);
                for (ServiceListener listener : listeners) {
                    try {
                        listener.onRun(configuration, environment, DropwizardTestSupport.this);
                    } catch (Exception ex) {
                        throw new RuntimeException("Error running app rule start listener", ex);
                    }
                }
            }
        };
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
