package com.yammer.dropwizard.db;

import org.apache.tomcat.dbcp.dbcp.PoolingDataSource;
import org.apache.tomcat.dbcp.pool.ObjectPool;

import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class ClosablePooledDataSource extends PoolingDataSource implements ClosableDataSource {
    private final ObjectPool pool;

    public ClosablePooledDataSource(ObjectPool pool) {
        super(pool);
        this.pool = pool;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Doesn't use java.util.logging");
    }

    @Override
    public void close() throws IOException {
        try {
            pool.close();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
