package io.dropwizard.migrations;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

public class TestMigrateConfiguration extends Configuration {

    private DataSourceFactory dataSource;

    public TestMigrateConfiguration(DataSourceFactory dataSource) {
        this.dataSource = dataSource;
    }

    public DataSourceFactory getDataSource() {
        return dataSource;
    }
}
