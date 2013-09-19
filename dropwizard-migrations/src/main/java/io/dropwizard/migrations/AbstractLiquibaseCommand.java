package io.dropwizard.migrations;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import liquibase.Liquibase;
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
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final DataSourceFactory dbConfig = strategy.getDataSourceFactory(configuration);
        dbConfig.setMaxSize(1);
        dbConfig.setMinSize(1);
        dbConfig.setInitialSize(1);

        try (CloseableLiquibase liquibase = openLiquibase(dbConfig, namespace)) {
            run(namespace, liquibase);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.err);
            throw e;
        }
    }

    private CloseableLiquibase openLiquibase(DataSourceFactory dataSourceFactory,
                                           Namespace namespace) throws ClassNotFoundException, SQLException, LiquibaseException {
        final ManagedDataSource dataSource = dataSourceFactory.build(new MetricRegistry(), "liquibase");
        final String migrationsFile = (String) namespace.get("migrations-file");
        if (migrationsFile == null) {
            return new CloseableLiquibase(dataSource);
        }
        return new CloseableLiquibase(dataSource, migrationsFile);
    }

    protected abstract void run(Namespace namespace, Liquibase liquibase) throws Exception;
}
