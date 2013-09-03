package com.codahale.dropwizard.testing.junit;

import java.util.Enumeration;

import net.sourceforge.argparse4j.inf.Namespace;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.cli.ServiceCommand;
import com.codahale.dropwizard.lifecycle.ServerLifecycleListener;
import com.codahale.dropwizard.server.ServerCommand;
import com.codahale.dropwizard.server.ServerCommand.JettyService;
import com.codahale.dropwizard.server.ServerConfiguration;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;

/**
 * A JUnit rule for starting and stopping your application at the start and end of a test class.
 * <p/>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).
 *
 * @param <C> the configuration type
 */
public class DropwizardAppRule<C extends ServerConfiguration> implements TestRule {

    private final Class<? extends Application<C>> applicationClass;
    private final String configPath;

    private C configuration;
    private Application<C> application;
    private Environment environment;
    private Server jettyServer;

    public DropwizardAppRule(Class<? extends Application<C>> applicationClass,
                             String configPath,
                             ConfigOverride... configOverrides) {
        this.applicationClass = applicationClass;
        this.configPath = configPath;
        for (ConfigOverride configOverride: configOverrides) {
            configOverride.addToSystemProperties();
        }
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                startIfRequired();
                try {
                    base.evaluate();
                } finally {
                    resetConfigOverrides();
                    jettyServer.stop();
                }
            }
        };
    }

    private void resetConfigOverrides() {
        for (Enumeration<?> props = System.getProperties().propertyNames(); props.hasMoreElements();) {
            String keyString = (String) props.nextElement();
            if (keyString.startsWith("dw.")) {
                System.clearProperty(keyString);
            }
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
                    environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
                                    @Override
                                    public void serverStarted(Service server) {
                                        if (server instanceof JettyService){
                                            jettyServer=((JettyService)server).getServer();
                                        }
                                    }
                                });
                    DropwizardAppRule.this.configuration = configuration;
                    DropwizardAppRule.this.environment = environment;
                    super.run(configuration, environment);
                }
            };

            application.initialize(bootstrap);
            final ServiceCommand<C> command = new ServerCommand<>(application);
            final Namespace namespace = new Namespace(ImmutableMap.<String, Object>of("file", configPath));
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
}
