package com.yammer.dropwizard.testing.junit;

import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.ServerCommand;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.lifecycle.ServerLifecycleListener;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DropwizardServiceRule<C extends Configuration> implements TestRule {

    private final Class<? extends Service<C>> serviceClass;
    private final String configPath;

    private C configuration;
    private Service<C> service;
    private Environment environment;
    private Server jettyServer;

    public DropwizardServiceRule(Class<? extends Service<C>> serviceClass, String configPath) {
        this.serviceClass = serviceClass;
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
            service = serviceClass.newInstance();

            final Bootstrap<C> bootstrap = new Bootstrap<C>(service) {
                @Override
                public void runWithBundles(C configuration, Environment environment) throws Exception {
                    environment.getLifecycleEnvironment().addServerLifecycleListener(new ServerLifecycleListener() {
                                    @Override
                                    public void serverStarted(Server server) {
                                        jettyServer = server;
                                    }
                                });
                    DropwizardServiceRule.this.configuration = configuration;
                    DropwizardServiceRule.this.environment = environment;
                    super.runWithBundles(configuration, environment);
                }
            };

            service.initialize(bootstrap);
            final ServerCommand<C> command = new ServerCommand<C>(service);
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
        return jettyServer.getConnectors()[0].getLocalPort();
    }

    @SuppressWarnings("unchecked")
    public <S extends Service<C>> S getService() {
        return (S) service;
    }

    public Environment getEnvironment() {
        return environment;
    }
}
