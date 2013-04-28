package com.codahale.dropwizard.jdbi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.dropwizard.db.DatabaseConfiguration;
import com.codahale.dropwizard.db.ManagedDataSource;
import com.codahale.dropwizard.db.ManagedDataSourceFactory;
import com.codahale.dropwizard.jdbi.args.OptionalArgumentFactory;
import com.codahale.dropwizard.jdbi.logging.LogbackLog;
import com.codahale.metrics.jdbi.InstrumentedTimingCollector;
import com.codahale.metrics.jdbi.strategies.DelegatingStatementNameStrategy;
import com.codahale.metrics.jdbi.strategies.NameStrategies;
import com.codahale.metrics.jdbi.strategies.StatementNameStrategy;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.slf4j.LoggerFactory;

import static com.codahale.metrics.MetricRegistry.name;

public class DBIFactory {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DBI.class);
    private static final String RAW_SQL = name(DBI.class, "raw-sql");

    private static class SanerNamingStrategy extends DelegatingStatementNameStrategy {
        private SanerNamingStrategy() {
            super(NameStrategies.CHECK_EMPTY,
                  NameStrategies.CONTEXT_CLASS,
                  NameStrategies.CONTEXT_NAME,
                  NameStrategies.SQL_OBJECT,
                  new StatementNameStrategy() {
                      @Override
                      public String getStatementName(StatementContext statementContext) {
                          return RAW_SQL;
                      }
                  });
        }
    }

    private final ManagedDataSourceFactory dataSourceFactory = new ManagedDataSourceFactory();

    public DBI build(Environment environment,
                     DatabaseConfiguration configuration,
                     String name) throws ClassNotFoundException {
        final ManagedDataSource dataSource = dataSourceFactory.build(environment.getMetricRegistry(),
                                                                     configuration,
                                                                     name);
        return build(environment, configuration, dataSource, name);
    }

    public DBI build(Environment environment,
                     DatabaseConfiguration configuration,
                     ManagedDataSource dataSource,
                     String name) {
        final String validationQuery = configuration.getValidationQuery();
        final DBI dbi = new DBI(dataSource);
        environment.getLifecycleEnvironment().manage(dataSource);
        environment.getAdminEnvironment().addHealthCheck(name, new DBIHealthCheck(dbi, validationQuery));
        dbi.setSQLLog(new LogbackLog(LOGGER, Level.TRACE));
        dbi.setTimingCollector(new InstrumentedTimingCollector(environment.getMetricRegistry(),
                                                               new SanerNamingStrategy()));
        if (configuration.isAutoCommentsEnabled()) {
            dbi.setStatementRewriter(new NamePrependingStatementRewriter(new ColonPrefixNamedParamStatementRewriter()));
        }
        dbi.registerArgumentFactory(new OptionalArgumentFactory(configuration.getDriverClass()));
        dbi.registerContainerFactory(new ImmutableListContainerFactory());
        dbi.registerContainerFactory(new ImmutableSetContainerFactory());
        dbi.registerContainerFactory(new OptionalContainerFactory());

        return dbi;
    }
}
