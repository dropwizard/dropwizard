package io.dropwizard.db;

import io.dropwizard.Configuration;

public interface DatabaseConfiguration<T extends Configuration> {
    DataSourceFactory getDataSourceFactory(T configuration);
}
