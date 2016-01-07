package io.dropwizard.db;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSourceProxy;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;

import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A {@link ManagedDataSource} which is backed by a Tomcat pooled {@link javax.sql.DataSource}.
 */
public class ManagedPooledDataSource extends DataSourceProxy implements ManagedDataSource {
    private final MetricRegistry metricRegistry;

    /**
     * Create a new data source with the given connection pool configuration.
     *
     * @param config the connection pool configuration
     */
    public ManagedPooledDataSource(PoolConfiguration config, MetricRegistry metricRegistry) {
        super(config);
        this.metricRegistry = metricRegistry;
    }

    // JDK6 has JDBC 4.0 which doesn't have this -- don't add @Override
    @SuppressWarnings("override")
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Doesn't use java.util.logging");
    }

    @Override
    public void start() throws Exception {
        final ConnectionPool connectionPool = createPool();
        metricRegistry.register(name(getClass(), connectionPool.getName(), "active"),
            (Gauge<Integer>) connectionPool::getActive);

        metricRegistry.register(name(getClass(), connectionPool.getName(), "idle"),
            (Gauge<Integer>) connectionPool::getIdle);

        metricRegistry.register(name(getClass(), connectionPool.getName(), "waiting"),
            (Gauge<Integer>) connectionPool::getWaitCount);

        metricRegistry.register(name(getClass(), connectionPool.getName(), "size"),
            (Gauge<Integer>) connectionPool::getSize);
    }

    @Override
    public void stop() throws Exception {
        close();
    }
}
