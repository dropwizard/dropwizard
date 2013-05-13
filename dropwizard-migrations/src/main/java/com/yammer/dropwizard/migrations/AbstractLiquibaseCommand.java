package com.yammer.dropwizard.migrations;

import com.google.common.base.Joiner;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.MultiDbConfigurationStrategy;
import liquibase.Liquibase;
import liquibase.exception.ValidationFailedException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public abstract class AbstractLiquibaseCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final MultiDbConfigurationStrategy<T> strategy;
    private final Class<T> configurationClass;
    private String schemaResourcePath;
    private final static String DATABASE_DEST = "database";

    protected AbstractLiquibaseCommand(String name,
                                       String description,
                                       MultiDbConfigurationStrategy<T> strategy,
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
        if (!strategy.getDatabaseNames().isEmpty()) {
            subparser.addArgument("--db", "--database")
                    .dest(DATABASE_DEST)
                    .help("Use the specified database. One of: " + Joiner.on(", ").join(strategy.getDatabaseNames()))
                    .required(strategy.hasDefault());
        }
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        String name = namespace.getString(DATABASE_DEST);
        final DatabaseConfiguration dbConfig = strategy.getDatabaseConfiguration(name, configuration);

        if (dbConfig == null) {
            String availableDatabases = Joiner.on(", ").join(strategy.getDatabaseNames());

            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Database name must be provided via --database because no default is configured. Available configurations are: " + availableDatabases);
            } else {
                throw new IllegalArgumentException(String.format("Unknown database configuration '%s'. Available configurations are: %s",
                        name,
                        availableDatabases

                ));
            }
        }

        dbConfig.setMaxSize(1);
        dbConfig.setMinSize(1);
        schemaResourcePath = dbConfig.getSchemaResourcePath();

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

    protected String getSchemaResourcePath() {
        return schemaResourcePath;
    }
}
