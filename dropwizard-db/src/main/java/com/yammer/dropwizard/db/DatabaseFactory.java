package com.yammer.dropwizard.db;

import com.yammer.dropwizard.config.Environment;
import org.apache.tomcat.dbcp.dbcp.DriverManagerConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolingDataSource;
import org.apache.tomcat.dbcp.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class DatabaseFactory {
    private final Environment environment;

    public DatabaseFactory(Environment environment) {
        this.environment = environment;
    }

    public Database build(DatabaseConfiguration configuration, String name) throws ClassNotFoundException {
        Class.forName(configuration.getDriverClass());
        final GenericObjectPool pool = buildPool(configuration);
        final DataSource dataSource = buildDataSource(configuration, pool);
        final Closeable closablePool = new Closeable() {
          public void close() throws IOException {
              try { pool.close(); }
              catch (Exception e) { throw new IOException(e); }
          }
        };
        final Database database = new Database(dataSource, closablePool, configuration.getValidationQuery());
        environment.manage(database);
        environment.addHealthCheck(new DatabaseHealthCheck(database, name));
        return database;
    }

    private static DataSource buildDataSource(DatabaseConfiguration connectionConfig, GenericObjectPool pool) {
        final Properties properties = new Properties();
        for (Map.Entry<String, String> property : connectionConfig.getProperties().entrySet()) {
            properties.setProperty(property.getKey(), property.getValue());
        }
        properties.setProperty("user", connectionConfig.getUser());
        properties.setProperty("password", connectionConfig.getPassword());

        final DriverManagerConnectionFactory factory = new DriverManagerConnectionFactory(connectionConfig.getUrl(),
                                                                                          properties);


        final PoolableConnectionFactory connectionFactory = new PoolableConnectionFactory(factory,
                                                                                          pool,
                                                                                          null,
                                                                                          connectionConfig.getValidationQuery(),
                                                                                          connectionConfig.isDefaultReadOnly(),
                                                                                          true);
        connectionFactory.setPool(pool);

        return new PoolingDataSource(pool);
    }

    private static GenericObjectPool buildPool(DatabaseConfiguration configuration) {
        final GenericObjectPool pool = new GenericObjectPool(null);
        pool.setMaxWait(configuration.getMaxWaitForConnection().toMilliseconds());
        pool.setMinIdle(configuration.getMinSize());
        pool.setMaxActive(configuration.getMaxSize());
        pool.setMaxIdle(configuration.getMaxSize());
        pool.setTestWhileIdle(configuration.isCheckConnectionWhileIdle());
        pool.setTimeBetweenEvictionRunsMillis(configuration.getCheckConnectionHealthWhenIdleFor().toMilliseconds());
        pool.setMinEvictableIdleTimeMillis(configuration.getCloseConnectionIfIdleFor()
                                                        .toMilliseconds());
        pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
        return pool;
    }
}
