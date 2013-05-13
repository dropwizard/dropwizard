package com.codahale.dropwizard.testing.junit;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.cli.ServerCommand;
import com.codahale.dropwizard.lifecycle.ServerLifecycleListener;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.google.common.collect.ImmutableMap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DropwizardAppRule<C extends Configuration> implements TestRule {

    private final Class<? extends Application<C>> applicationClass;
    private final String configPath;

    private C configuration;
    private Application<C> application;
    private Environment environment;
    private Server jettyServer;

    public DropwizardAppRule(Class<? extends Application<C>> applicationClass, String configPath) {
        this.applicationClass = applicationClass;
        this.configPath = configPath;
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
                    jettyServer.stop();
                }
            }
        };
    }

    private void startIfRequired() {
        if (jettyServer != null) {
            return;
        }

        try {
            application = applicationClass.newInstance();

            final Bootstrap<C> bootstrap = new Bootstrap<C>(application) {
                @Override
                public void runWithBundles(C configuration, Environment environment) throws Exception {
                    environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
                                    @Override
                                    public void serverStarted(Server server) {
                                        jettyServer = server;
                                    }
                                });
                    DropwizardAppRule.this.configuration = configuration;
                    DropwizardAppRule.this.environment = environment;
                    super.runWithBundles(configuration, environment);
                }
            };

            application.initialize(bootstrap);
            final ServerCommand<C> command = new ServerCommand<>(application);
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

    @SuppressWarnings("unchecked")
    public <S extends Application<C>> S getApplication() {
        return (S) application;
    }

    public Environment getEnvironment() {
        return environment;
    }
}
