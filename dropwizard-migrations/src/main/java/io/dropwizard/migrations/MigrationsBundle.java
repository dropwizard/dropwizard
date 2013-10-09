package io.dropwizard.migrations;

import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Generics;

public abstract class MigrationsBundle<T extends Configuration> implements Bundle, DatabaseConfiguration<T> {
    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        final Class<T> klass = Generics.getTypeParameter(getClass(), Configuration.class);
        bootstrap.addCommand(new DbCommand<>(this, klass));
    }

    @Override
    public final void run(Environment environment) {
        // nothing doing
    }
}
