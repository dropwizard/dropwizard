package com.yammer.dropwizard.db;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.StringMapper;

import com.yammer.metrics.core.HealthCheck;

public class DatabaseHealthCheck extends HealthCheck {
    private final Database database;
    private final String checkSql;

    public DatabaseHealthCheck(Database database, String name, String checkSql) {
        super(name + "-db");
        this.database = database;
        this.checkSql = checkSql;
    }

    @Override
    protected Result check() throws Exception {
        Handle h = database.open();
        String val = h.createQuery(this.checkSql).map(StringMapper.FIRST).first();
        h.close();
        return Result.healthy("Check query returned: " + val);
    }
}
