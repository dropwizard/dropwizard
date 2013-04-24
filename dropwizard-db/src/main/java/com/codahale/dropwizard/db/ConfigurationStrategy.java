package com.codahale.dropwizard.db;

import com.codahale.dropwizard.config.Configuration;

public interface ConfigurationStrategy<T extends Configuration> {
    DatabaseConfiguration getDatabaseConfiguration(T configuration);
}
