package com.codahale.dropwizard.db;

import com.codahale.dropwizard.Configuration;
import com.google.common.collect.ImmutableList;

public interface RouteCapableDatabaseConfiguration<T extends Configuration> {
    /**
     * First entry will be default route.
     * 
     * @param configuration
     *            service configuration
     * @return the list of {@link DataSourceRoute}
     */
    ImmutableList<DataSourceRoute> getDataSourceRoutes(T configuration);
}
