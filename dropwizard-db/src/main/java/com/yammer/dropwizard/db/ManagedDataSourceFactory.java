package com.yammer.dropwizard.db;

import org.apache.tomcat.dbcp.dbcp.DriverManagerConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.pool.impl.GenericObjectPool;

import java.util.Map;
import java.util.Properties;

public class ManagedDataSourceFactory {
    public ManagedDataSource build(DatabaseConfiguration configuration) throws ClassNotFoundException {
        Class.forName(configuration.getDriverClass());
        final GenericObjectPool pool = buildPool(configuration);

        final Properties properties = new Properties();
        for (Map.Entry<String, String> property : configuration.getProperties().entrySet()) {
            properties.setProperty(property.getKey(), property.getValue());
        }
        properties.setProperty("user", configuration.getUser());
        properties.setProperty("password", configuration.getPassword());

        final DriverManagerConnectionFactory factory = new DriverManagerConnectionFactory(
                configuration.getUrl(),
                properties);


        final PoolableConnectionFactory connectionFactory = new PoolableConnectionFactory(factory,
                                                                                          pool,
                                                                                          null,
                                                                                          configuration.getValidationQuery(),
                                                                                          configuration.getConnectionInitializationStatements(),
                                                                                          configuration.isDefaultReadOnly(),
                                                                                          true);
        connectionFactory.setPool(pool);

        return new ManagedPooledDataSource(pool);
    }

    private GenericObjectPool buildPool(DatabaseConfiguration configuration) {
        final GenericObjectPool pool = new GenericObjectPool(null);
        pool.setMaxWait(configuration.getMaxWaitForConnection().toMilliseconds());
        pool.setMinIdle(configuration.getMinSize());
        pool.setMaxActive(configuration.getMaxSize());
        pool.setMaxIdle(configuration.getMaxSize());
        pool.setTestWhileIdle(configuration.isCheckConnectionWhileIdle());
        pool.setTimeBetweenEvictionRunsMillis(configuration.getCheckConnectionHealthWhenIdleFor()
                                                           .toMilliseconds());
        pool.setMinEvictableIdleTimeMillis(configuration.getCloseConnectionIfIdleFor()
                                                        .toMilliseconds());
        pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
        return pool;
    }
}
