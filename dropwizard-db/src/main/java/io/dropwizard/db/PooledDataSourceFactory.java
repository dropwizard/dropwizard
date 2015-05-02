package io.dropwizard.db;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import io.dropwizard.util.Duration;

import java.util.Map;

/**
 * Interface of a factory that produces JDBC data sources
 * backed by the connection pool.
 */
public interface PooledDataSourceFactory {

    /**
     * Whether ORM tools allowed to add comments to SQL queries.
     *
     * @return {@code true}, if allowed
     */
    boolean isAutoCommentsEnabled();

    /**
     * Returns the configuration properties for ORM tools.
     *
     * @return configuration properties as a map
     */
    Map<String, String> getProperties();

    /**
     * Returns the timeout for awaiting a response from the database
     * during connection health checks.
     *
     * @return the timeout as {@code Duration}
     */
    Optional<Duration> getHealthCheckValidationTimeout();

    /**
     * Returns the SQL query, which is being used for the database
     * connection health check.
     *
     * @return the SQL query as a string
     */
    String getHealthCheckValidationQuery();

    /**
     * Returns the Java class of the database driver.
     *
     * @return the JDBC driver class as a string
     */
    String getDriverClass();

    /**
     * Configures the pool as a single connection pool.
     * It's useful for tools that use only one database connection,
     * such as database migrations.
     */
    void asSingleConnectionPool();

    /**
     * Builds a new JDBC data source backed by the connection pool
     * and managed by Dropwizard.
     *
     * @param metricRegistry the application metric registry
     * @param name           name of the connection pool
     * @return a new JDBC data source as {@code ManagedDataSource}
     */
    ManagedDataSource build(MetricRegistry metricRegistry, String name);
}
