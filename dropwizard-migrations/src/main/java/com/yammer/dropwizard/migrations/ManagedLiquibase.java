package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.lifecycle.Managed;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;

public class ManagedLiquibase implements Managed {
    private final ManagedDataSource managedDataSource;
    private final DatabaseConnection dbConnection;
    private final Liquibase liquibase;

    public ManagedLiquibase(ManagedDataSource managedDataSource,
                            DatabaseConnection dbConnection,
                            Liquibase liquibase) {
        this.managedDataSource = managedDataSource;
        this.dbConnection = dbConnection;
        this.liquibase = liquibase;
    }

    public Liquibase getLiquibase() {
        return liquibase;
    }

    public void migrate() throws LiquibaseException {
        liquibase.update("");
    }

    public void dropAll() throws DatabaseException, LockException {
        liquibase.dropAll();
    }

    @Override
    public void start() throws Exception {
        // ALREADY STARTED
    }

    @Override
    public void stop() throws Exception {
        try {
            dbConnection.close();
        } finally {
            managedDataSource.stop();
        }
    }
}