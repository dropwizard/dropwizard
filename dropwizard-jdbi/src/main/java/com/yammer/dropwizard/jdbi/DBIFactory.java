package com.yammer.dropwizard.jdbi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.db.ManagedDataSourceFactory;
import com.yammer.dropwizard.jdbi.args.OptionalArgumentFactory;
import com.yammer.dropwizard.jdbi.logging.LogbackLog;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.jdbi.InstrumentedTimingCollector;
import com.yammer.metrics.jdbi.strategies.DelegatingStatementNameStrategy;
import com.yammer.metrics.jdbi.strategies.NameStrategies;
import com.yammer.metrics.jdbi.strategies.StatementNameStrategy;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.slf4j.LoggerFactory;

public class DBIFactory {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DBI.class);
    private static final MetricName RAW_SQL = new MetricName(DBI.class, "raw-sql");

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

    private final ManagedDataSourceFactory dataSourceFactory = new ManagedDataSourceFactory();

    public DBI build(Environment environment,
                     DatabaseConfiguration configuration,
                     String name) throws ClassNotFoundException {
        final ManagedDataSource dataSource = dataSourceFactory.build(configuration);
        return build(environment, configuration, dataSource, name);
    }

    public DBI build(Environment environment,
                     DatabaseConfiguration configuration,
                     ManagedDataSource dataSource,
                     String name) {
        final String validationQuery = configuration.getValidationQuery();
        final DBI dbi = new DBI(dataSource);
        environment.manage(dataSource);
        environment.addHealthCheck(new DBIHealthCheck(dbi, name, validationQuery));
        dbi.setSQLLog(new LogbackLog(LOGGER, Level.TRACE));
        dbi.setTimingCollector(new InstrumentedTimingCollector(Metrics.defaultRegistry(),
                                                               new SanerNamingStrategy()));
        dbi.setStatementRewriter(new NamePrependingStatementRewriter(new ColonPrefixNamedParamStatementRewriter()));
        dbi.registerArgumentFactory(new OptionalArgumentFactory(configuration.getDriverClass()));
        dbi.registerContainerFactory(new ImmutableListContainerFactory());
        dbi.registerContainerFactory(new ImmutableSetContainerFactory());
        dbi.registerContainerFactory(new OptionalContainerFactory());

        return dbi;
    }
}
