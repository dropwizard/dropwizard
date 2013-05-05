package com.codahale.dropwizard.testing;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.Service;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.After;
import org.junit.Before;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

public abstract class HealthCheckTest {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private final Map<String, HealthCheck> healthChecksByName = Maps.newHashMap();
    private TestCommand<Configuration> command;

    protected abstract void setUpHealthChecks() throws Exception;

    protected void addHealthCheck(String name, HealthCheck healthCheck) {
        healthChecksByName.put(name, healthCheck);
    }

    protected String getContent() throws Exception {
        URL url = new URL("http://localhost:18081/healthcheck");
        return CharStreams.toString(new InputStreamReader(url.openStream(), Charsets.UTF_8));
    }

    @Before
    public final void setUp() throws Exception {
        setUpHealthChecks();

        Service<Configuration> service = new Service<Configuration>() {
            @Override
            public void initialize(Bootstrap<Configuration> bootstrap) {
            }

            @Override
            public void run(Configuration configuration, Environment environment) throws Exception {
                for (String name : healthChecksByName.keySet()) {
                    environment.admin().addHealthCheck(name, healthChecksByName.get(name));
                }
            }
        };

        Map<String, Object> attrs = Maps.newHashMap();
        command = new TestCommand<>(service);
        command.run(new Bootstrap<>(service), new Namespace(attrs));
    }

    @After
    public void tearDown() throws Exception {
        command.stop();
    }
}
