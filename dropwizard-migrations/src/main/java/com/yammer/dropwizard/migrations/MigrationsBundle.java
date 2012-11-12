package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import com.yammer.dropwizard.util.Generics;

public abstract class MigrationsBundle<T extends Configuration> implements Bundle, ConfigurationStrategy<T> {
    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        @SuppressWarnings("unchecked")
        final Class<T> klass = (Class<T>) Generics.getTypeParameter(getClass(), Configuration.class);
        bootstrap.addCommand(new DbCommand<T>(this, klass));
    }

    @Override
    public final void run(Environment environment) {
        // nothing doing
    }
}
