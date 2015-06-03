package io.dropwizard.migrations;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;

public class TestMigrationDatabaseConfiguration implements DatabaseConfiguration<TestMigrationConfiguration> {

    @Override
    public DataSourceFactory getDataSourceFactory(TestMigrationConfiguration configuration) {
        return configuration.getDataSource();
    }
}
