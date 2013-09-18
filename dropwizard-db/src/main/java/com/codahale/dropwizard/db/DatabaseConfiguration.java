package com.codahale.dropwizard.db;

import com.codahale.dropwizard.Configuration;

public interface DatabaseConfiguration<T extends Configuration> {
    DataSourceFactory getDataSourceFactory(T configuration);
}
