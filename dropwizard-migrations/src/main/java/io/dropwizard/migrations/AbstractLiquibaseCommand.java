package io.dropwizard.migrations;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.sql.SQLException;

public abstract class AbstractLiquibaseCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final DatabaseConfiguration<T> strategy;
    private final Class<T> configurationClass;

    protected AbstractLiquibaseCommand(String name,
                                       String description,
                                       DatabaseConfiguration<T> strategy,
                                       Class<T> configurationClass) {
        super(name, description);
        this.strategy = strategy;
        this.configurationClass = configurationClass;
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
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final PooledDataSourceFactory dbConfig = strategy.getDataSourceFactory(configuration);
        dbConfig.asSingleConnectionPool();

        try (final CloseableLiquibase liquibase = openLiquibase(dbConfig, namespace)) {
            run(namespace, liquibase);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.err);
            throw e;
        }
    }

    private CloseableLiquibase openLiquibase(final PooledDataSourceFactory dataSourceFactory, final Namespace namespace)
            throws SQLException, LiquibaseException {
        final CloseableLiquibase liquibase;
        final ManagedDataSource dataSource = dataSourceFactory.build(new MetricRegistry(), "liquibase");

        final String migrationsFile = namespace.getString("migrations-file");
        if (migrationsFile == null) {
            liquibase = new CloseableLiquibase(dataSource);
        } else {
            liquibase = new CloseableLiquibase(dataSource, migrationsFile);
        }

        final Database database = liquibase.getDatabase();
        final String catalogName = namespace.getString("catalog");
        final String schemaName = namespace.getString("schema");

        if (database.supportsCatalogs() && catalogName != null) {
            database.setDefaultCatalogName(catalogName);
            database.setOutputDefaultCatalog(true);
        }
        if (database.supportsSchemas() && schemaName != null) {
            database.setDefaultSchemaName(schemaName);
            database.setOutputDefaultSchema(true);
        }

        return liquibase;
    }

    protected abstract void run(Namespace namespace, Liquibase liquibase) throws Exception;
}
