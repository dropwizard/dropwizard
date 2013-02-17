package com.yammer.dropwizard.testing.junit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.yammer.dropwizard.Bundle;
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

public class DropwizardServiceRule<S extends Service, C extends Configuration> implements TestRule {

    private final Class<S> serviceClass;
    private final Class<C> configClass;
    private final String configPath;

    private Server jettyServer;

    public DropwizardServiceRule(Class<S> serviceClass, Class<C> configClass, String configPath) {
        this.serviceClass = serviceClass;
        this.configClass = configClass;
        this.configPath = configPath;
    }

    @Override
    public Statement apply(final Statement baseStatement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                startIfRequired();
                try {
                    baseStatement.evaluate();
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
            Service<C> service = serviceClass.newInstance();
            final Bootstrap<C> bootstrap = new Bootstrap<C>(service);
            bootstrap.addBundle(new Bundle() {
                @Override
                public void initialize(Bootstrap<?> bootstrap) {
                }

                @Override
                public void run(Environment environment) {
                    environment.addServerLifecycleListener(new ServerLifecycleListener() {
                        @Override
                        public void serverStarted(Server server) {
                            jettyServer = server;
                        }
                    });
                }
            });
            service.initialize(bootstrap);
            ServerCommand<C> command = new ServerCommand<C>(service, false);
            Namespace namespace = new Namespace(ImmutableMap.<String, Object>of("file", configPath));
            command.run(bootstrap, namespace);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
