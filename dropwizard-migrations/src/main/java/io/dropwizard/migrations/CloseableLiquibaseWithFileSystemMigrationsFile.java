package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

import java.sql.SQLException;

public class CloseableLiquibaseWithFileSystemMigrationsFile extends CloseableLiquibase implements AutoCloseable {

    public CloseableLiquibaseWithFileSystemMigrationsFile(ManagedDataSource dataSource, String file) throws LiquibaseException, SQLException {
        super(file,
              new FileSystemResourceAccessor(),
              new JdbcConnection(dataSource.getConnection()),
              dataSource);
    }

}
