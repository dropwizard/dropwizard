package io.dropwizard.cdi;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class WeldBundle implements Bundle {

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // NO OP
    }

    @Override
    public void run(Environment environment) {
        environment.lifecycle().manage(new ManagedWeld());
    }
}
