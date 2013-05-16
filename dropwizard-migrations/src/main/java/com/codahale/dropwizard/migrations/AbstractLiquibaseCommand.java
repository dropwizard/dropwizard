package com.codahale.dropwizard.migrations;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.cli.ConfiguredCommand;
import com.codahale.dropwizard.db.DatabaseConfiguration;
import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.setup.Bootstrap;
import liquibase.Liquibase;
import liquibase.exception.ValidationFailedException;
import net.sourceforge.argparse4j.inf.Namespace;

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
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final DataSourceFactory dbConfig = strategy.getDataSourceFactory(configuration);
        dbConfig.setMaxSize(1);
        dbConfig.setMinSize(1);
        dbConfig.setInitialSize(1);

        ManagedLiquibase managedLiquibase = null;
        try {
            managedLiquibase = new ManagedLiquibase(dbConfig);
            run(namespace, managedLiquibase);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.err);
            throw e;
        } finally {
            if (managedLiquibase != null) {
                managedLiquibase.stop();
            }
        }
    }

    protected abstract void run(Namespace namespace, Liquibase liquibase) throws Exception;
}
