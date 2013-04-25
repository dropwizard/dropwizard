package com.yammer.dropwizard.testing;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.EnvironmentCommand;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.ServerFactory;
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
        server = new ServerFactory(configuration.getHttpConfiguration(),
                environment.getName()).buildServer(environment);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
