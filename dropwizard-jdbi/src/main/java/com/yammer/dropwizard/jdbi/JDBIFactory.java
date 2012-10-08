package com.yammer.dropwizard.jdbi;

import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.ClosableDataSource;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.PooledDataSourceFactory;

public class JDBIFactory {
    private final Environment environment;

    public JDBIFactory(Environment environment) {
        this.environment = environment;
    }

    public JDBI build(DatabaseConfiguration configuration, String name) throws ClassNotFoundException {
        final ClosableDataSource dataSource = new PooledDataSourceFactory(configuration).build();
        final JDBI JDBI = new JDBI(dataSource, configuration.getValidationQuery());
        environment.manage(JDBI);
        environment.addHealthCheck(new JDBIHealthCheck(JDBI, name));
        return JDBI;
    }
}
