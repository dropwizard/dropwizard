package com.yammer.dropwizard.db;

import org.apache.tomcat.dbcp.dbcp.PoolingDataSource;
import org.apache.tomcat.dbcp.pool.ObjectPool;

import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class ManagedPooledDataSource extends PoolingDataSource implements ManagedDataSource {
    private final ObjectPool pool;

    public ManagedPooledDataSource(ObjectPool pool) {
        super(pool);
        this.pool = pool;
    }

    @Override
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
