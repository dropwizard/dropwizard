package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.db.ManagedDataSourceFactory;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.SQLException;

public class ManagedLiquibaseFactory {
    private ManagedLiquibaseFactory() { /* singleton */ }

    public static ManagedLiquibase create(DatabaseConfiguration dbConfig) throws ClassNotFoundException,
                                                                                 LiquibaseException,
                                                                                 SQLException {
        final ManagedDataSource dataSource = new ManagedDataSourceFactory(dbConfig).build();
        final DatabaseConnection conn = new JdbcConnection(dataSource.getConnection());
        final Liquibase liquibase = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), conn);
        liquibase.validate();
        return new ManagedLiquibase(dataSource, conn, liquibase);
    }
}