package io.dropwizard.migrations;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.setup.Bootstrap;

public abstract class MigrationsBundle<T extends Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {
    private static final String DEFAULT_NAME = "db";
    private static final String DEFAULT_MIGRATIONS_FILE = "migrations.xml";

    @Override
    @SuppressWarnings("unchecked")
    public final void initialize(Bootstrap<?> bootstrap) {
        final Class<T> klass = (Class<T>) bootstrap.getApplication().getConfigurationClass();
        bootstrap.addCommand(new DbCommand<>(name(), this, klass, getMigrationsFileName()));
    }

    public String getMigrationsFileName() {
        return DEFAULT_MIGRATIONS_FILE;
    }

    public String name() {
        return DEFAULT_NAME;
    }
}
