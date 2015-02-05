package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;

import java.sql.SQLException;

public class CloseableLiquibase extends Liquibase implements AutoCloseable {
    private static final String DEFAULT_MIGRATIONS_FILE = "migrations.xml";
    private final ManagedDataSource dataSource;

    public CloseableLiquibase(ManagedDataSource dataSource) throws LiquibaseException, SQLException {
        super(DEFAULT_MIGRATIONS_FILE,
              new ClassLoaderResourceAccessor(),
              new JdbcConnection(dataSource.getConnection()));
        this.dataSource = dataSource;
    }

    public CloseableLiquibase(ManagedDataSource dataSource, String file) throws LiquibaseException, SQLException {
        super(file,
              new FileSystemResourceAccessor(),
              new JdbcConnection(dataSource.getConnection()));
        this.dataSource = dataSource;
    }

    @Override
    public void close() throws Exception {
        try {
            database.close();
        } finally {
            dataSource.stop();
        }
    }
}
