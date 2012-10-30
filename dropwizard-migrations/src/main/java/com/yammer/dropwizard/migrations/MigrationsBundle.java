package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import com.yammer.dropwizard.util.Generics;

public abstract class MigrationsBundle<T extends Configuration>
        extends Bundle implements ConfigurationStrategy<T> {
    @Override
    @SuppressWarnings("unchecked")
    public void initialize(Bootstrap<?> bootstrap) {
        final Class<T> klass = (Class<T>) Generics.getTypeParameter(getClass(), Configuration.class);
        bootstrap.addCommand(new DbCommand<T>(this, klass));
    }
}
