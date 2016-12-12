package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

import java.sql.SQLException;

public class CloseableLiquibaseWithFileSystemMigrationsFile extends CloseableLiquibase implements AutoCloseable {

    public CloseableLiquibaseWithFileSystemMigrationsFile(ManagedDataSource dataSource, Database database, String file) throws LiquibaseException, SQLException {
        super(file,
              new FileSystemResourceAccessor(),
              database,
              dataSource);
    }

}
