package com.codahale.dropwizard.db;

import com.codahale.dropwizard.Configuration;

public interface ConfigurationStrategy<T extends Configuration> {
    DataSourceFactory getDatabaseFactory(T configuration);
}
