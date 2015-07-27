package io.dropwizard.cli;

import io.dropwizard.HttpApplication;
import io.dropwizard.HttpConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.setup.HttpEnvironment;
import net.sourceforge.argparse4j.inf.Namespace;

public abstract class HttpEnvironmentCommand<T extends HttpConfiguration> extends EnvironmentCommand<T> {

    /**
     * Creates a new http environment command.
     *
     * @param application the application providing this command
     * @param name        the name of the command, used for command line invocation
     * @param description a description of the command's purpose
     */
    protected HttpEnvironmentCommand(HttpApplication<T> application, String name, String description) {
        super(application, name, description);
    }

    @Override
    protected final HttpEnvironment createEnvironment(Bootstrap<T> bootstrap) {
        return new HttpEnvironment(bootstrap.getApplication().getName(),
                                   bootstrap.getObjectMapper(),
                                   bootstrap.getValidatorFactory().getValidator(),
                                   bootstrap.getMetricRegistry(),
                                   bootstrap.getClassLoader());
    }

    @Override
    protected final void run(Environment environment, Namespace namespace, T configuration) throws Exception {
        if (environment instanceof HttpEnvironment) {
            run((HttpEnvironment) environment, namespace, configuration);
        } else {
            throw new IllegalStateException("HttpEnvironmentCommand must use HttpEnvironment");
        }
    }

    protected abstract void run(HttpEnvironment environment, Namespace namespace, T configuration) throws Exception;
}
