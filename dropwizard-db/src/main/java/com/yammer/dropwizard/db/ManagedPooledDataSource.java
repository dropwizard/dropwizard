package com.yammer.dropwizard.db;

import org.apache.tomcat.dbcp.dbcp.PoolingDataSource;
import org.apache.tomcat.dbcp.pool.ObjectPool;

import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * A {@link PoolingDataSource} which is also {@link ManagedDataSource}.
 */
public class ManagedPooledDataSource extends PoolingDataSource implements ManagedDataSource {
    private final ObjectPool pool;

    /**
     * Create a new data source with the given connection pool.
     *
     * @param pool    a connection pool
     */
    public ManagedPooledDataSource(ObjectPool pool) {
        super(pool);
        this.pool = pool;
    }

    // JDK6 has JDBC 4.0 which doesn't have this -- don't add @Override
    @SuppressWarnings("override")
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Doesn't use java.util.logging");
    }

    @Override
    public void start() throws Exception {
        // already started
    }

    @Override
    public void stop() throws Exception {
        pool.close();
    }
}
