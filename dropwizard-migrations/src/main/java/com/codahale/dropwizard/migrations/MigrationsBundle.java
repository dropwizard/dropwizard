package com.codahale.dropwizard.migrations;

import com.codahale.dropwizard.Bundle;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.db.DatabaseConfiguration;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.dropwizard.util.Generics;

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
