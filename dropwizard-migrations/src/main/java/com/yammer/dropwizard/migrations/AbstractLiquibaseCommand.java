package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import liquibase.exception.ValidationFailedException;
import net.sourceforge.argparse4j.inf.Namespace;

public abstract class AbstractLiquibaseCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final ConfigurationStrategy<T> strategy;
    private final Class<T> configurationClass;

    protected AbstractLiquibaseCommand(String name,
                                       String description,
                                       ConfigurationStrategy<T> strategy,
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
        final DatabaseConfiguration dbConfig = strategy.getDatabaseConfiguration(configuration);
        dbConfig.setMaxSize(1);
        dbConfig.setMinSize(1);

        ManagedLiquibase managedLiquibase = null;
        try {
            managedLiquibase = new ManagedLiquibase(dbConfig);
            run(namespace, managedLiquibase);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.err);
        } finally {
            if (managedLiquibase != null) {
                managedLiquibase.stop();
            }
        }
    }

    protected abstract void run(Namespace namespace, Liquibase liquibase) throws Exception;
}
