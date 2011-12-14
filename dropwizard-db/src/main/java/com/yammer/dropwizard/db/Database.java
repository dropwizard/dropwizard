package com.yammer.dropwizard.db;

import com.yammer.dropwizard.db.args.OptionalArgumentFactory;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.jdbi.InstrumentedTimingCollector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tomcat.dbcp.pool.ObjectPool;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.logging.Log4JLog;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import javax.sql.DataSource;
import java.sql.SQLException;

public class Database extends DBI implements Managed {
    public interface Ping {
        @SqlQuery("SELECT 1")
        public Integer ping();
    }

    private static final Logger LOGGER = Logger.getLogger(Database.class);

    private final ObjectPool pool;
    private final Ping ping;

    public Database(DataSource dataSource, ObjectPool pool) {
        super(dataSource);
        this.pool = pool;
        this.ping = onDemand(Ping.class);
        setSQLLog(new Log4JLog(LOGGER, Level.TRACE));
        setTimingCollector(new InstrumentedTimingCollector(Metrics.defaultRegistry()));
        setStatementRewriter(new NamePrependingStatementRewriter());
        setStatementLocator(new ScopedStatementLocator());
        registerArgumentFactory(new OptionalArgumentFactory());
        registerContainerFactory(new ImmutableListContainerFactory());
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
        final Integer value = ping.ping();
        if (!Integer.valueOf(1).equals(value)) {
            throw new SQLException("Expected 1 from 'SELECT 1', got " + value);
        }
    }
}
