package com.codahale.dropwizard.db;

import com.codahale.dropwizard.util.Duration;
import com.codahale.metrics.MetricRegistry;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.util.Map;
import java.util.Properties;

public class ManagedDataSourceFactory {
    public ManagedDataSource build(MetricRegistry metricRegistry,
                                   DatabaseConfiguration configuration,
                                   String name) throws ClassNotFoundException {
        final Properties properties = new Properties();
        for (Map.Entry<String, String> property : configuration.getProperties().entrySet()) {
            properties.setProperty(property.getKey(), property.getValue());
        }

        final PoolProperties poolConfig = new PoolProperties();
        poolConfig.setAbandonWhenPercentageFull(configuration.getAbandonWhenPercentageFull());
        poolConfig.setAlternateUsernameAllowed(configuration.isAlternateUsernameAllowed());
        poolConfig.setCommitOnReturn(configuration.getCommitOnReturn());
        poolConfig.setDbProperties(properties);
        poolConfig.setDefaultAutoCommit(configuration.getAutoCommitByDefault());
        poolConfig.setDefaultCatalog(configuration.getDefaultCatalog());
        poolConfig.setDefaultReadOnly(configuration.getReadOnlyByDefault());
        poolConfig.setDefaultTransactionIsolation(configuration.getDefaultTransactionIsolation()
                                                               .getValue());
        poolConfig.setDriverClassName(configuration.getDriverClass());
        poolConfig.setFairQueue(configuration.getUseFairQueue());
        poolConfig.setInitialSize(configuration.getInitialSize());
        poolConfig.setInitSQL(configuration.getInitializationQuery());
        poolConfig.setLogAbandoned(configuration.getLogAbandonedQueries());
        poolConfig.setLogValidationErrors(configuration.getLogValidationErrors());
        poolConfig.setMaxActive(configuration.getMaxSize());
        poolConfig.setMaxIdle(configuration.getMaxSize());
        poolConfig.setMinIdle(configuration.getMinSize());
        for (Duration duration : configuration.getMaxConnectionAge().asSet()) {
            poolConfig.setMaxAge(duration.toMilliseconds());
        }
        poolConfig.setMaxWait((int) configuration.getMaxWaitForConnection().toMilliseconds());
        poolConfig.setMinEvictableIdleTimeMillis((int) configuration.getMinIdleTime().toMilliseconds());
        poolConfig.setName(name);
        poolConfig.setUrl(configuration.getUrl());
        poolConfig.setUsername(configuration.getUser());
        poolConfig.setPassword(configuration.getPassword());
        poolConfig.setTestWhileIdle(configuration.getCheckConnectionWhileIdle());
        poolConfig.setValidationQuery(configuration.getValidationQuery());
        poolConfig.setTestOnBorrow(configuration.getCheckConnectionOnBorrow());
        poolConfig.setTestOnConnect(configuration.getCheckConnectionOnConnect());
        poolConfig.setTestOnReturn(configuration.getCheckConnectionOnReturn());
        poolConfig.setTimeBetweenEvictionRunsMillis((int) configuration.getEvictionInterval().toMilliseconds());
        poolConfig.setValidationInterval(1);

        return new ManagedPooledDataSource(poolConfig, metricRegistry);
    }
}
