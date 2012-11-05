package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.db.ManagedDataSourceFactory;
import com.yammer.dropwizard.lifecycle.Managed;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.SQLException;

public class ManagedLiquibase extends Liquibase implements Managed {
    private static class ManagedJdbcConnection extends JdbcConnection implements Managed {
        private final ManagedDataSource dataSource;

        @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
        private ManagedJdbcConnection(ManagedDataSource dataSource) throws SQLException {
            super(dataSource.getConnection());
            this.dataSource = dataSource;
        }

        @Override
        public void start() throws Exception {
            // ALREADY STARTED
        }

        @Override
        public void stop() throws Exception {
            dataSource.stop();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    public ManagedLiquibase(DatabaseConfiguration configuration) throws LiquibaseException, ClassNotFoundException, SQLException {
        super("migrations.xml",
              new ClassLoaderResourceAccessor(),
              new ManagedJdbcConnection(new ManagedDataSourceFactory().build(configuration)));
    }

    @Override
    public void start() throws Exception {
        // ALREADY STARTED
    }

    @Override
    public void stop() throws Exception {
        final DatabaseConnection connection = getDatabase().getConnection();
        if (connection instanceof ManagedJdbcConnection) {
            ((ManagedJdbcConnection) connection).stop();
        }
    }
}
