package io.dropwizard.jdbi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.codahale.metrics.jdbi.InstrumentedTimingCollector;
import com.codahale.metrics.jdbi.strategies.DelegatingStatementNameStrategy;
import com.codahale.metrics.jdbi.strategies.NameStrategies;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jdbi.args.GuavaOptionalArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalInstantArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalJodaTimeArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalLocalDateArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalLocalDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalOffsetTimeArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalZonedTimeArgumentFactory;
import io.dropwizard.jdbi.args.InstantArgumentFactory;
import io.dropwizard.jdbi.args.InstantMapper;
import io.dropwizard.jdbi.args.JodaDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.JodaDateTimeMapper;
import io.dropwizard.jdbi.args.LocalDateArgumentFactory;
import io.dropwizard.jdbi.args.LocalDateMapper;
import io.dropwizard.jdbi.args.LocalDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.LocalDateTimeMapper;
import io.dropwizard.jdbi.args.OffsetDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.OffsetDateTimeMapper;
import io.dropwizard.jdbi.args.OptionalArgumentFactory;
import io.dropwizard.jdbi.args.OptionalDoubleArgumentFactory;
import io.dropwizard.jdbi.args.OptionalDoubleMapper;
import io.dropwizard.jdbi.args.OptionalInstantArgumentFactory;
import io.dropwizard.jdbi.args.OptionalIntArgumentFactory;
import io.dropwizard.jdbi.args.OptionalIntMapper;
import io.dropwizard.jdbi.args.OptionalJodaTimeArgumentFactory;
import io.dropwizard.jdbi.args.OptionalLocalDateArgumentFactory;
import io.dropwizard.jdbi.args.OptionalLocalDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.OptionalLongArgumentFactory;
import io.dropwizard.jdbi.args.OptionalLongMapper;
import io.dropwizard.jdbi.args.OptionalOffsetDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.OptionalZonedDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.ZonedDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.ZonedDateTimeMapper;
import io.dropwizard.jdbi.logging.LogbackLog;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.DBI;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.TimeZone;

import static com.codahale.metrics.MetricRegistry.name;

public class DBIFactory {

    /**
     * An SLF4J created instance of a LogBack logger for use with created DBI instances.
     */
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DBI.class);

    private static final String RAW_SQL = name(DBI.class, "raw-sql");

    private static class SanerNamingStrategy extends DelegatingStatementNameStrategy {
        private SanerNamingStrategy() {
            super(NameStrategies.CHECK_EMPTY,
                NameStrategies.CONTEXT_CLASS,
                NameStrategies.CONTEXT_NAME,
                NameStrategies.SQL_OBJECT,
                statementContext -> RAW_SQL);
        }
    }

    /**
     * Get a time zone of a database
     * <p>
     * <p>Override this method to specify a time zone of a database
     * to use in {@link io.dropwizard.jdbi.args.JodaDateTimeMapper} and
     * {@link io.dropwizard.jdbi.args.JodaDateTimeArgument}</p>
     * <p>
     * <p>It's needed for cases when the database operates in a different
     * time zone then the application and it doesn't use the SQL type
     * `TIMESTAMP WITH TIME ZONE`. It such cases information about the
     * time zone should explicitly passed to the JDBC driver</p>
     *
     * @return a time zone of a database
     */
    protected Optional<TimeZone> databaseTimeZone() {
        return Optional.empty();
    }


    /**
     * Build a fully configured DBI instance managed by the DropWizard lifecycle
     * with the configured health check; this method should not be overridden
     * (instead, override {@link #configure(DBI, PooledDataSourceFactory)} and/or
     * {@link #newInstance(ManagedDataSource)})
     *
     * @param environment
     * @param configuration
     * @param name
     * @return A fully configured {@link DBI} object using a managed data source
     * based on the specified environment and configuration
     * @see #build(Environment, PooledDataSourceFactory, ManagedDataSource,
     * String)
     */
    public DBI build(Environment environment,
                     PooledDataSourceFactory configuration,
                     String name) {
        final ManagedDataSource dataSource = configuration.build(environment.metrics(), name).getWriteDataSource();
        return build(environment, configuration, dataSource, name);
    }

    /**
     * Build a fully configured DBI instance managed by the DropWizard lifecycle
     * with the configured health check; this method should not be overridden
     * (instead, override {@link #configure(DBI, PooledDataSourceFactory)} and/or
     * {@link #newInstance(ManagedDataSource)})
     *
     * This will be an instance connected to your read URL. If you do not configure a read URL
     * Then this is that same as running {@link #build(Environment, PooledDataSourceFactory, String)}
     *
     * @param environment
     * @param configuration
     * @param name
     * @return A fully configured {@link DBI} object using a managed data source
     * based on the specified environment and configuration
     * @see #build(Environment, PooledDataSourceFactory, ManagedDataSource,
     * String)
     */
    public DBI buildReadOnly(Environment environment,
                     PooledDataSourceFactory configuration,
                     String name) {
        final ManagedDataSource dataSource = configuration.build(environment.metrics(), name).getReadDataSource();
        return build(environment, configuration, dataSource, name);
    }

    /**
     * Build a fully configured DBI instance managed by the DropWizard lifecycle
     * with the configured health check; this method should not be overridden
     * (instead, override {@link #configure(DBI, PooledDataSourceFactory)} and/or
     * {@link #newInstance(ManagedDataSource)})
     *
     * @param environment
     * @param configuration
     * @param dataSource
     * @param name
     * @return A fully configured {@link DBI} object
     */
    public DBI build(Environment environment,
                     PooledDataSourceFactory configuration,
                     ManagedDataSource dataSource,
                     String name) {

        // Create the instance
        final DBI dbi = this.newInstance(dataSource);

        // Manage the data source that created this instance.
        environment.lifecycle().manage(dataSource);

        // Setup the required health checks.
        final String validationQuery = configuration.getValidationQuery();
        environment.healthChecks().register(name, new DBIHealthCheck(
            environment.getHealthCheckExecutorService(),
            configuration.getValidationQueryTimeout().orElseGet(() -> Duration.seconds(5)),
            dbi,
            validationQuery));

        // Setup logging.
        dbi.setSQLLog(new LogbackLog(LOGGER, Level.TRACE));

        // Setup the timing collector
        dbi.setTimingCollector(new InstrumentedTimingCollector(environment.metrics(),
            new SanerNamingStrategy()));

        if (configuration.isAutoCommentsEnabled()) {
            dbi.setStatementRewriter(new NamePrependingStatementRewriter(new ColonPrefixNamedParamStatementRewriter()));
        }

        // Add the default argument and column mapper factories.
        this.configure(dbi, configuration);

        return dbi;
    }

    /**
     * This creates a vanilla DBI instance based on the specified data source;
     * this can be overridden if required
     *
     * @param dataSource
     * @return
     */
    protected DBI newInstance(final ManagedDataSource dataSource) {
        return new DBI(dataSource);
    }

    /**
     * This method sets the default column mapper and argument factories
     * <p>
     * if this is overridden it is strongly recommend that
     * {@code super.configure(dbi, configuration)} is invoked before any other
     * changes are made if you intend to use the default as a base so that the
     * customized argument settings will supersede the defaults
     * </p>
     *
     * @param dbi
     * @param configuration
     */
    protected void configure(final DBI dbi, final PooledDataSourceFactory configuration) {
        final String driverClazz = configuration.getDriverClass();

        dbi.registerArgumentFactory(new GuavaOptionalArgumentFactory(driverClazz));
        dbi.registerArgumentFactory(new OptionalArgumentFactory(driverClazz));
        dbi.registerArgumentFactory(new OptionalDoubleArgumentFactory());
        dbi.registerArgumentFactory(new OptionalIntArgumentFactory());
        dbi.registerArgumentFactory(new OptionalLongArgumentFactory());
        dbi.registerColumnMapper(new OptionalDoubleMapper());
        dbi.registerColumnMapper(new OptionalIntMapper());
        dbi.registerColumnMapper(new OptionalLongMapper());
        dbi.registerContainerFactory(new ImmutableListContainerFactory());
        dbi.registerContainerFactory(new ImmutableSetContainerFactory());
        dbi.registerContainerFactory(new GuavaOptionalContainerFactory());
        dbi.registerContainerFactory(new OptionalContainerFactory());

        final Optional<TimeZone> timeZone = databaseTimeZone();
        dbi.registerArgumentFactory(new JodaDateTimeArgumentFactory(timeZone));
        dbi.registerArgumentFactory(new LocalDateArgumentFactory());
        dbi.registerArgumentFactory(new LocalDateTimeArgumentFactory());
        dbi.registerArgumentFactory(new InstantArgumentFactory(timeZone));
        dbi.registerArgumentFactory(new OffsetDateTimeArgumentFactory(timeZone));
        dbi.registerArgumentFactory(new ZonedDateTimeArgumentFactory(timeZone));

        // Should be registered after GuavaOptionalArgumentFactory to be
        // processed first
        dbi.registerArgumentFactory(new GuavaOptionalJodaTimeArgumentFactory(timeZone));
        dbi.registerArgumentFactory(new GuavaOptionalLocalDateArgumentFactory());
        dbi.registerArgumentFactory(new GuavaOptionalLocalDateTimeArgumentFactory());
        dbi.registerArgumentFactory(new GuavaOptionalInstantArgumentFactory(timeZone));
        dbi.registerArgumentFactory(new GuavaOptionalOffsetTimeArgumentFactory(timeZone));
        dbi.registerArgumentFactory(new GuavaOptionalZonedTimeArgumentFactory(timeZone));

        // Should be registered after OptionalArgumentFactory to be processed
        // first
        dbi.registerArgumentFactory(new OptionalJodaTimeArgumentFactory(timeZone));
        dbi.registerArgumentFactory(new OptionalLocalDateArgumentFactory());
        dbi.registerArgumentFactory(new OptionalLocalDateTimeArgumentFactory());
        dbi.registerArgumentFactory(new OptionalInstantArgumentFactory(timeZone));
        dbi.registerArgumentFactory(new OptionalOffsetDateTimeArgumentFactory(timeZone));
        dbi.registerArgumentFactory(new OptionalZonedDateTimeArgumentFactory(timeZone));

        dbi.registerColumnMapper(new JodaDateTimeMapper(timeZone));
        dbi.registerColumnMapper(new LocalDateMapper());
        dbi.registerColumnMapper(new LocalDateTimeMapper());
        dbi.registerColumnMapper(new InstantMapper(timeZone));
        dbi.registerColumnMapper(new OffsetDateTimeMapper(timeZone));
        dbi.registerColumnMapper(new ZonedDateTimeMapper(timeZone));
    }
}
