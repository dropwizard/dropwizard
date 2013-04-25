package com.yammer.dropwizard.testing;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.setup.AdminEnvironment;
import com.yammer.metrics.core.HealthCheck;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.After;
import org.junit.Before;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public abstract class HealthCheckTest {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private final Set<HealthCheck> singletons = Sets.newHashSet();

    private TestCommand<Configuration> command;

    protected abstract void setUpHealthChecks() throws Exception;

    protected void addHealthCheck(HealthCheck healthCheck) {
        singletons.add(healthCheck);
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
                configuration.getHttpConfiguration().setAdminPort(18081);
                for (HealthCheck singleton : singletons) {
                    environment.getAdminEnvironment().addHealthCheck(singleton);
                }
            }
        };

        Map<String, Object> attrs = Maps.newHashMap();
        command = new TestCommand<Configuration>(service);
        command.run(new Bootstrap<Configuration>(service),
                new Namespace(attrs));
    }

    @After
    public void tearDown() throws Exception {
        command.stop();
    }
}
