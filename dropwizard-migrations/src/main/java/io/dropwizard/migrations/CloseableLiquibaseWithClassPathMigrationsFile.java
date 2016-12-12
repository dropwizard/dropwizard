package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.SQLException;

public class CloseableLiquibaseWithClassPathMigrationsFile extends CloseableLiquibase implements AutoCloseable {

    public CloseableLiquibaseWithClassPathMigrationsFile(ManagedDataSource dataSource, Database database, String file) throws LiquibaseException, SQLException {
        super(file,
              new ClassLoaderResourceAccessor(),
              database,
              dataSource);
    }

}
