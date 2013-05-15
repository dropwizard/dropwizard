package com.codahale.dropwizard.migrations;

import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.db.ManagedDataSource;
import com.codahale.dropwizard.lifecycle.Managed;
import com.codahale.metrics.MetricRegistry;
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
            close();
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

    public ManagedLiquibase(DataSourceFactory configuration) throws LiquibaseException, ClassNotFoundException, SQLException {
        super("migrations.xml",
              new ClassLoaderResourceAccessor(),
              new ManagedJdbcConnection(configuration.build(new MetricRegistry(), "liquibase")));
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
