package com.yammer.dropwizard.jdbi;

import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.ClosableDataSource;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.PooledDataSourceFactory;

public class DatabaseFactory {
    private final Environment environment;

    public DatabaseFactory(Environment environment) {
        this.environment = environment;
    }

    public Database build(DatabaseConfiguration configuration, String name) throws ClassNotFoundException {
        final ClosableDataSource dataSource = new PooledDataSourceFactory(configuration).build();
        final Database database = new Database(dataSource, configuration.getValidationQuery());
        environment.manage(database);
        environment.addHealthCheck(new DatabaseHealthCheck(database, name));
        return database;
    }
}
