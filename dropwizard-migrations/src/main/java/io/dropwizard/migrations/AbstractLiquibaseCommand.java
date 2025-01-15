package io.dropwizard.migrations;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsArgs;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;

public abstract class AbstractLiquibaseCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final DatabaseConfiguration<T> strategy;
    private final Class<T> configurationClass;
    private final String migrationsFileName;

    protected AbstractLiquibaseCommand(String name,
                                       String description,
                                       DatabaseConfiguration<T> strategy,
                                       Class<T> configurationClass,
                                       String migrationsFileName) {
        super(name, description);
        this.strategy = strategy;
        this.configurationClass = configurationClass;
        this.migrationsFileName = migrationsFileName;
    }

    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("--migrations")
                .dest("migrations-file")
                .help("the file containing the Liquibase migrations for the application");

        subparser.addArgument("--catalog")
                .dest("catalog")
                .help("Specify the database catalog (use database default if omitted)");

        subparser.addArgument("--schema")
                .dest("schema")
                .help("Specify the database schema (use database default if omitted)");

        subparser.addArgument("--analytics-enabled")
                .setDefault(false)
                .dest("analytics-enabled")
                .help("This turns on analytics gathering for that single occurrence of a command.");
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    protected void run(@Nullable Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final PooledDataSourceFactory dbConfig = strategy.getDataSourceFactory(configuration);
        dbConfig.asSingleConnectionPool();

        try (final CloseableLiquibase liquibase = openLiquibase(dbConfig, namespace)) {
            run(namespace, liquibase);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.err);
            throw e;
        }
    }

    CloseableLiquibase openLiquibase(final PooledDataSourceFactory dataSourceFactory, final Namespace namespace)
            throws SQLException, LiquibaseException {
        final CloseableLiquibase liquibase;
        final ManagedDataSource dataSource = dataSourceFactory.build(new MetricRegistry(), "liquibase");
        final Database database = createDatabase(dataSource, namespace);
        final String migrationsFile = namespace.getString("migrations-file");
        if (migrationsFile == null) {
            liquibase = new CloseableLiquibaseWithClassPathMigrationsFile(dataSource, database, migrationsFileName);
        } else {
            liquibase = new CloseableLiquibaseWithFileSystemMigrationsFile(dataSource, database, migrationsFile);
        }

        final Boolean analyticsEnabled = namespace.getBoolean("analytics-enabled");
        try {
            Map<String, Object> values = Map.of(
                "liquibase.analytics.enabled", analyticsEnabled != null && analyticsEnabled,
                "liquibase.analytics.logLevel", Level.FINEST // OFF is mapped to SLF4J ERROR level...
            );
            Scope.enter(values);
        } catch (Exception e) {
            throw new LiquibaseException(e);
        }

        return liquibase;
    }

    private Database createDatabase(
        ManagedDataSource dataSource,
        Namespace namespace
    ) throws SQLException, LiquibaseException {
        final DatabaseConnection conn = new JdbcConnection(dataSource.getConnection());
        final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn);

        final String catalogName = namespace.getString("catalog");
        final String schemaName = namespace.getString("schema");

        if (database.supports(Catalog.class) && catalogName != null) {
            database.setDefaultCatalogName(catalogName);
            database.setOutputDefaultCatalog(true);
        }
        if (database.supports(Schema.class) && schemaName != null) {
            database.setDefaultSchemaName(schemaName);
            database.setOutputDefaultSchema(true);
        }

        return database;
    }

    protected abstract void run(Namespace namespace, Liquibase liquibase) throws Exception;
}
