package com.yammer.dropwizard.db;

import com.yammer.dropwizard.lifecycle.Managed;
import org.apache.tomcat.dbcp.pool.ObjectPool;
import org.skife.jdbi.v2.DBI;

import javax.sql.DataSource;

public class Database extends DBI implements Managed {
    private final ObjectPool pool;

    public Database(DataSource dataSource, ObjectPool pool) {
        super(dataSource);
        this.pool = pool;
    }

    @Override
    public void start() throws Exception {
        // already started, man
    }

    @Override
    public void stop() throws Exception {
        pool.close();
    }
}
