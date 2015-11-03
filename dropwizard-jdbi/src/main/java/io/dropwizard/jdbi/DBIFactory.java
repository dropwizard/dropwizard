package io.dropwizard.jdbi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.codahale.metrics.jdbi.InstrumentedTimingCollector;
import com.codahale.metrics.jdbi.strategies.DelegatingStatementNameStrategy;
import com.codahale.metrics.jdbi.strategies.NameStrategies;
import com.codahale.metrics.jdbi.strategies.StatementNameStrategy;
import com.google.common.base.Optional;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi.args.JodaDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.JodaDateTimeMapper;
import io.dropwizard.jdbi.args.OptionalArgumentFactory;
import io.dropwizard.jdbi.args.OptionalJodaTimeArgumentFactory;
import io.dropwizard.jdbi.logging.LogbackLog;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;

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

    /**
     * Get a time zone of a database
     *
     * <p/>Override this method to specify a time zone of a database
     * to use in {@link io.dropwizard.jdbi.args.JodaDateTimeMapper} and
     * {@link io.dropwizard.jdbi.args.JodaDateTimeArgument}</p>
     *
     * <p>It's needed for cases when the database operates in a different
     * time zone then the application and it doesn't use the SQL type
     * `TIMESTAMP WITH TIME ZONE`. It such cases information about the
     * time zone should explicitly passed to the JDBC driver</p>
     *
     * @return a time zone of a database
     */
    protected Optional<TimeZone> databaseTimeZone() {
        return Optional.absent();
    }

    public DBI build(Environment environment,
                     PooledDataSourceFactory configuration,
                     String name) {
        final ManagedDataSource dataSource = configuration.build(environment.metrics(), name);
        return build(environment, configuration, dataSource, name);
    }

    public DBI build(Environment environment,
                     PooledDataSourceFactory configuration,
                     ManagedDataSource dataSource,
                     String name) {
        final String validationQuery = configuration.getValidationQuery();
        final DBI dbi = new DBI(dataSource);
        environment.lifecycle().manage(dataSource);
        environment.healthChecks().register(name, new DBIHealthCheck(
                environment.getHealthCheckExecutorService(),
                configuration.getValidationQueryTimeout().or(Duration.seconds(5)),
                dbi,
                validationQuery));
        dbi.setSQLLog(new LogbackLog(LOGGER, Level.TRACE));
        dbi.setTimingCollector(new InstrumentedTimingCollector(environment.metrics(),
                new SanerNamingStrategy()));
        if (configuration.isAutoCommentsEnabled()) {
            dbi.setStatementRewriter(new NamePrependingStatementRewriter(new ColonPrefixNamedParamStatementRewriter()));
        }
        dbi.registerArgumentFactory(new OptionalArgumentFactory(configuration.getDriverClass()));
        dbi.registerContainerFactory(new ImmutableListContainerFactory());
        dbi.registerContainerFactory(new ImmutableSetContainerFactory());
        dbi.registerContainerFactory(new OptionalContainerFactory());

        final Optional<TimeZone> timeZone = databaseTimeZone();
        dbi.registerArgumentFactory(new JodaDateTimeArgumentFactory(timeZone));
        // Should be registered after OptionalArgumentFactory to be processed first
        dbi.registerArgumentFactory(new OptionalJodaTimeArgumentFactory(timeZone));
        dbi.registerMapper(new JodaDateTimeMapper(timeZone));

        return dbi;
    }
}
