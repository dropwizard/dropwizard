package com.yammer.dropwizard.db;

import com.yammer.dropwizard.lifecycle.Managed;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tomcat.dbcp.pool.ObjectPool;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.logging.Log4JLog;
import org.skife.jdbi.v2.util.IntegerMapper;

import javax.sql.DataSource;
import java.sql.SQLException;

public class Database extends DBI implements Managed {
    private static final Logger LOGGER = Logger.getLogger(Database.class);

    private final ObjectPool pool;

    public Database(DataSource dataSource, ObjectPool pool) {
        super(dataSource);
        this.pool = pool;
        setSQLLog(new Log4JLog(LOGGER, Level.TRACE));
    }

    @Override
    public void start() throws Exception {
        // already started, man
    }

    @Override
    public void stop() throws Exception {
        pool.close();
    }

    public void ping() throws SQLException {
        final Handle handle = open();
        try {
            final Integer first = handle.createQuery("SELECT 1").map(IntegerMapper.FIRST).first();
            if (!Integer.valueOf(1).equals(first)) {
                throw new SQLException("Expected 1 from 'SELECT 1', got " + first);
            }
        } finally {
            handle.close();
        }
    }
}
