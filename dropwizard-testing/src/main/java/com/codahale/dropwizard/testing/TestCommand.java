package com.codahale.dropwizard.testing;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.Service;
import com.codahale.dropwizard.cli.EnvironmentCommand;
import com.codahale.dropwizard.server.DefaultServerFactory;
import com.codahale.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;

public class TestCommand<T extends Configuration> extends EnvironmentCommand<T> {
    private final Class<T> configurationClass;
    private Server server;

    public TestCommand(Service<T> service) {
        super(service, "server", "Test server for health checks");
        this.configurationClass = service.getConfigurationClass();
    }

    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    protected void run(Environment environment, Namespace namespace, T configuration) throws Exception {
        DefaultServerFactory serverFactory = new DefaultServerFactory();
        serverFactory.setAdminPort(18081);
        server = serverFactory.build(
                environment.getName(),
                environment.metrics(),
                environment.healthChecks(),
                environment.lifecycle(),
                environment.getServletContext(),
                environment.getJerseyServletContainer(),
                environment.getAdminContext(),
                environment.jersey(),
                environment.getObjectMapper(),
                environment.getValidator()
        );
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
