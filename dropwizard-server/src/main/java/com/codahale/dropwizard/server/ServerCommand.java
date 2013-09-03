package com.codahale.dropwizard.server;

import java.util.concurrent.Executor;

import javax.validation.Validation;

import org.eclipse.jetty.server.Server;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.cli.ServiceCommand;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;

public class ServerCommand<T extends ServerConfiguration> extends ServiceCommand<T>
{
    public ServerCommand(Application<T> application)
    {
        super(application, "server", "Runs the Dropwizard application as an HTTP server");
    }
    
    @Override
    protected Environment createEnvironment( Bootstrap<T> bootstrap )
    {
        return new ServerEnvironment(bootstrap.getApplication().getName(),
                                                        bootstrap.getObjectMapper(),
                                                        Validation.buildDefaultValidatorFactory()
                                                                  .getValidator(),
                                                        bootstrap.getMetricRegistry(),
                                                        bootstrap.getClassLoader());
    }

    @Override
    protected Service buildService(Environment environment, T configuration)
    {
        return serviceFromServer( configuration.getServerFactory().build(environment));
    }
    
    
    public static interface JettyService extends Service
    {
        public Server getServer();
    }
    
    private static final class JettyServiceImpl extends AbstractIdleService implements JettyService
    {
        private final Server server;

        private JettyServiceImpl(Server server)
        {
            this.server = server;
        }
        
        @Override
        protected Executor executor()
        {
            return MoreExecutors.sameThreadExecutor();
        }
        
        @Override
        protected void startUp() throws Exception
        {
            this.server.start();
        }

        @Override
        protected void shutDown() throws Exception
        {
            this.server.stop();
        }
        
        public Server getServer()
        {
            return server;
        }
    }
    
    private static JettyService serviceFromServer(final Server server)
    {
        return new JettyServiceImpl(server);
    }
}