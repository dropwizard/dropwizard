package com.yammer.dropwizard.jdbi;

import com.yammer.metrics.core.HealthCheck;

public class JDBIHealthCheck extends HealthCheck {
    private final JDBI JDBI;

    public JDBIHealthCheck(JDBI JDBI, String name) {
        super(name + "-db");
        this.JDBI = JDBI;
    }

    @Override
    protected Result check() throws Exception {
        JDBI.ping();
        return Result.healthy();
    }
}
