package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

public abstract class CloseableLiquibase extends Liquibase implements AutoCloseable {
    private final ManagedDataSource dataSource;

    CloseableLiquibase(
            String changeLogFile, ResourceAccessor resourceAccessor, Database database, ManagedDataSource dataSource) {
        super(changeLogFile, resourceAccessor, database);
        this.dataSource = dataSource;
    }

    protected CloseableLiquibase(
            String changeLogFile,
            ResourceAccessor resourceAccessor,
            DatabaseConnection conn,
            ManagedDataSource dataSource)
            throws LiquibaseException, SQLException {
        super(changeLogFile, resourceAccessor, conn);
        this.dataSource = dataSource;
    }

    @Override
    public void close() throws LiquibaseException {
        LiquibaseException exception = null;
        try {
            super.close();
        } finally {
            try {
                dataSource.stop();
            } catch (Exception e) {
                exception = new LiquibaseException(e);
            }
        }

        if (exception != null) {
            throw exception;
        }
    }
}
