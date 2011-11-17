package com.yammer.dropwizard.db;

import com.yammer.dropwizard.config.Environment;
import org.apache.tomcat.dbcp.dbcp.DriverManagerConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolingDataSource;
import org.apache.tomcat.dbcp.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yammer.dropwizard.db.DatabaseConfiguration.DatabaseConnectionConfiguration;

public class DatabaseFactory {
    private final DatabaseConfiguration config;

    public DatabaseFactory(DatabaseConfiguration config) {
        this.config = config;
    }

    public Database build(String name, Environment environment) throws ClassNotFoundException {
        final DatabaseConnectionConfiguration connectionConfig = config.getConnection(name);
        Class.forName(connectionConfig.getDriverClass());
        checkNotNull(connectionConfig, "%s is not the name of a configured connection.", name);
        final GenericObjectPool pool = buildPool(connectionConfig);
        final DataSource dataSource = buildDataSource(connectionConfig, pool);
        final Database database = new Database(dataSource, pool);
        environment.manage(database);
        return database;
    }

    private DataSource buildDataSource(DatabaseConnectionConfiguration connectionConfig, GenericObjectPool pool) {
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
                                                                                          false,
                                                                                          true);
        connectionFactory.setPool(pool);

        return new PoolingDataSource(pool);
    }

    private GenericObjectPool buildPool(DatabaseConnectionConfiguration connectionConfig) {
        final GenericObjectPool pool = new GenericObjectPool(null);
        pool.setMaxWait(connectionConfig.getMaxWaitForConnection().toMilliseconds());
        pool.setMinIdle(connectionConfig.getMinSize());
        pool.setMaxActive(connectionConfig.getMaxSize());
        pool.setMaxIdle(connectionConfig.getMaxSize());
        pool.setTestWhileIdle(connectionConfig.checkConnectionWhileIdle());
        pool.setTimeBetweenEvictionRunsMillis(connectionConfig.getCheckConnectionHealthWhenIdleFor().toMilliseconds());
        pool.setMinEvictableIdleTimeMillis(connectionConfig.getCloseConnectionIfIdleFor().toMilliseconds());
        return pool;
    }
}
