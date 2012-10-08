package com.yammer.dropwizard.jdbi;

import com.yammer.metrics.core.HealthCheck;

public class DatabaseHealthCheck extends HealthCheck {
    private final Database database;

    public DatabaseHealthCheck(Database database, String name) {
        super(name + "-db");
        this.database = database;
    }

    @Override
    protected Result check() throws Exception {
        database.ping();
        return Result.healthy();
    }
}
