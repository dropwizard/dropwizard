package com.yammer.dropwizard.jdbi;

import com.codahale.metrics.health.HealthCheck;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class DBIHealthCheck extends HealthCheck {
    private final DBI dbi;
    private final String validationQuery;

    public DBIHealthCheck(DBI dbi, String validationQuery) {
        this.dbi = dbi;
        this.validationQuery = validationQuery;
    }

    @Override
    protected Result check() throws Exception {
        final Handle handle = dbi.open();
        try {
            handle.execute(validationQuery);
        } finally {
            handle.close();
        }
        return Result.healthy();
    }
}
