package com.yammer.dropwizard.db;

import com.yammer.metrics.core.HealthCheck;

public class DatabaseHealthCheck extends HealthCheck {
    private final Database database;
    private final String name;
    
    public DatabaseHealthCheck(Database database, String name) {
        this.database = database;
        this.name = name;
    }

    @Override
    public String name() {
        return name + "-db";
    }

    @Override
    public Result check() throws Exception {
        database.ping();
        return Result.healthy();
    }
}
