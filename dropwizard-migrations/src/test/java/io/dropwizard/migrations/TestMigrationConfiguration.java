package io.dropwizard.migrations;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

public class TestMigrationConfiguration extends Configuration {

    private DataSourceFactory dataSource;

    public TestMigrationConfiguration(DataSourceFactory dataSource) {
        this.dataSource = dataSource;
    }

    public DataSourceFactory getDataSource() {
        return dataSource;
    }
}
