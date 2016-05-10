package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.SQLException;

public class CloseableLiquibaseWithClassPathMigrationsFile extends CloseableLiquibase implements AutoCloseable {

    public CloseableLiquibaseWithClassPathMigrationsFile(ManagedDataSource dataSource, String file) throws LiquibaseException, SQLException {
        super(file,
              new ClassLoaderResourceAccessor(),
              new JdbcConnection(dataSource.getConnection()),
              dataSource);
    }

}
