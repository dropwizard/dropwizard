package io.dropwizard.core.cli;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses and validates the application's configuration.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 */
public class CheckCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckCommand.class);

    private final Class<T> configurationClass;

    public CheckCommand(Application<T> application) {
        super("check", "Parses and validates the configuration file");
        this.configurationClass = application.getConfigurationClass();
    }

    /*
     * Since we don't subclass CheckCommand, we need a concrete reference to the configuration
     * class.
     */
    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    protected void run(Bootstrap<T> bootstrap,
                       Namespace namespace,
                       T configuration) throws Exception {
        LOGGER.info("Configuration is OK");
    }

    /* The stacktrace is redundant as the message contains the yaml error location */
    @Override
    public void onError(Cli cli, Namespace namespace, Throwable e) {
        cli.getStdErr().println(e.getMessage());
    }
}
