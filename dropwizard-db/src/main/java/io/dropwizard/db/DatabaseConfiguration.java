package io.dropwizard.db;

public interface DatabaseConfiguration<T> {
    PooledDataSourceFactory getDataSourceFactory(T configuration);
}
