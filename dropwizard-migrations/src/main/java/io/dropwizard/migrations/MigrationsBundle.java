package io.dropwizard.migrations;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.db.DatabaseConfiguration;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public abstract class MigrationsBundle<T extends Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {
    private static final String DEFAULT_NAME = "db";
    private static final String DEFAULT_MIGRATIONS_FILE = "migrations.xml";

    @Override
    @SuppressWarnings("unchecked")
    public final void initialize(Bootstrap<?> bootstrap) {
        final Class<T> klass = (Class<T>) bootstrap.getApplication().getConfigurationClass();
        bootstrap.addCommand(new DbCommand<>(name(), this, klass, getMigrationsFileName(), getScopedObjects()));
    }

    public String getMigrationsFileName() {
        return DEFAULT_MIGRATIONS_FILE;
    }

    public String name() {
        return DEFAULT_NAME;
    }

    /**
     * If overridden, enters a new {@link liquibase.Scope}, in which the provided objects are available.
     *
     * @return the objects introduced in the created child {@link liquibase.Scope}
     */
    @Nullable
    public Map<String, Object> getScopedObjects() {
        return null;
    }
}
