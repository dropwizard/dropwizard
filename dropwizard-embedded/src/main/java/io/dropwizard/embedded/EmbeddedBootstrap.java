package io.dropwizard.embedded;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

/**
 * A class for bootstrapping a {@link Service} with Bundles.
 * @see io.dropwizard.setup.Bootstrap
 */
public class EmbeddedBootstrap<T extends Configuration> extends Bootstrap<T> {
    private String name;

    /**
     * Creates a new {@link Bootstrap} for the given application.
     *
     * @param service a Dropwizard embedded {@link Service}
     */
    public EmbeddedBootstrap(Service service) {
        super(null);
        this.name = service.getName();
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @Override
    public Application<T> getApplication() {
        throw new UnsupportedOperationException("not applicable");
    }

    @Override
    public void addCommand(ConfiguredCommand<T> command) {
        throw new UnsupportedOperationException("not applicable");
    }
}
