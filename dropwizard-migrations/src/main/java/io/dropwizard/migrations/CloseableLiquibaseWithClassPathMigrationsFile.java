package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.SQLException;

public class CloseableLiquibaseWithClassPathMigrationsFile extends CloseableLiquibase implements AutoCloseable {

    CloseableLiquibaseWithClassPathMigrationsFile(
        ManagedDataSource dataSource,
        Database database,
        String file
    ) throws LiquibaseException, SQLException {
        super(file,
              new ClassLoaderResourceAccessor(),
              database,
              dataSource);
    }

    public CloseableLiquibaseWithClassPathMigrationsFile(
        ManagedDataSource dataSource,
        String file
    ) throws LiquibaseException, SQLException {
        this(dataSource,
            DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection())),
            file);
    }
}
