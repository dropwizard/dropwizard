package com.yammer.dropwizard.jdbi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.yammer.dropwizard.db.ClosableDataSource;
import com.yammer.dropwizard.jdbi.args.OptionalArgumentFactory;
import com.yammer.dropwizard.jdbi.logging.LogbackLog;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.jdbi.InstrumentedTimingCollector;
import com.yammer.metrics.jdbi.strategies.DelegatingStatementNameStrategy;
import com.yammer.metrics.jdbi.strategies.NameStrategies;
import com.yammer.metrics.jdbi.strategies.StatementNameStrategy;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class JDBI extends DBI implements Managed {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(JDBI.class);
    private static final MetricName RAW_SQL = new MetricName(JDBI.class, "raw-sql");
    private static class SanerNamingStrategy extends DelegatingStatementNameStrategy {
        private SanerNamingStrategy() {
            super(NameStrategies.CHECK_EMPTY,
                  NameStrategies.CONTEXT_CLASS,
                  NameStrategies.CONTEXT_NAME,
                  NameStrategies.SQL_OBJECT,
                  new StatementNameStrategy() {
                      @Override
                      public MetricName getStatementName(StatementContext statementContext) {
                          return RAW_SQL;
                      }
                  });
        }
    }

    private final ClosableDataSource dataSource;
    private final String validationQuery;

    public JDBI(ClosableDataSource dataSource, String validationQuery) {
        super(dataSource);
        this.dataSource = dataSource;
        this.validationQuery = validationQuery;
        setSQLLog(new LogbackLog(LOGGER, Level.TRACE));
        setTimingCollector(new InstrumentedTimingCollector(Metrics.defaultRegistry(),
                                                           new SanerNamingStrategy()));
        setStatementRewriter(new NamePrependingStatementRewriter(new ColonPrefixNamedParamStatementRewriter()));
        registerArgumentFactory(new OptionalArgumentFactory());
        registerContainerFactory(new ImmutableListContainerFactory());
    }

    @Override
    public void start() throws Exception {
        // already started, man
    }

    @Override
    public void stop() throws Exception {
        dataSource.close();
    }

    public void ping() throws SQLException {
        final Handle handle = open();
        try {
            handle.execute(validationQuery);
        } finally {
            handle.close();
        }
    }
}
