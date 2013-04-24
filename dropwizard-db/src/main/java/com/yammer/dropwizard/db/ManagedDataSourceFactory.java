package com.yammer.dropwizard.db;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.apache.tomcat.dbcp.dbcp.DriverManagerConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.pool.impl.GenericObjectPool;

import java.util.Map;
import java.util.Properties;

import static com.codahale.metrics.MetricRegistry.name;

public class ManagedDataSourceFactory {
    public ManagedDataSource build(MetricRegistry metricRegistry,
                                   DatabaseConfiguration configuration) throws ClassNotFoundException {
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

        setupGauges(metricRegistry, pool, configuration.getUrl());

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

    private void setupGauges(MetricRegistry metricRegistry, final GenericObjectPool pool, String scope) {

        metricRegistry.register(name(ManagedPooledDataSource.class, scope, "numActive"),
                                new Gauge<Integer>() {
                                    @Override
                                    public Integer getValue() {
                                        return pool.getNumActive();
                                    }
                                });

        metricRegistry.register(name(ManagedPooledDataSource.class, scope, "numIdle"),
                                new Gauge<Integer>() {

                                    @Override
                                    public Integer getValue() {
                                        return pool.getNumIdle();
                                    }
                                });
    }
}
