package com.codahale.dropwizard.cli;
import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.setup.Environment;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * Runs a application as an {@link Service}.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 */
public abstract class ServiceCommand<T extends Configuration> extends EnvironmentCommand<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCommand.class);

    private final Class<T> configurationClass;
    
    public ServiceCommand(Application<T> application, String name, String description) {
        super(application, name, description);
        this.configurationClass = application.getConfigurationClass();
    }

    public ServiceCommand(Application<T> application) {
        this( application, "service","Runs the Dropwizard application as a service");
    }

    /*
     * Since we don't subclass ServiceCommand, we need a concrete reference to the configuration
     * class.
     */
    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    protected abstract Service buildService(Environment environment, T configuration);

    @Override
    protected final void run(Environment environment, Namespace namespace, T configuration) throws Exception {        
        final Service service = buildService(environment, configuration);
        environment.lifecycle().attach(service);
        
        try {
            service.startAndWait();
        }
        catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception)e.getCause();
            }
            else {
                throw e;
            }
        }
        catch(RuntimeException e) 
        {
            // a service wrapped exception
            if (e.getClass() == RuntimeException.class 
                && e.getMessage()== null 
                && e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            else {
                throw e;
            }
        }
        catch (Exception e) {
            LOGGER.error("Unable to start server, shutting down", e);
            service.stopAndWait();
            throw e;
        }
    }
}