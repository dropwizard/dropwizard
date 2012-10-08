package com.yammer.dropwizard.db.migrations;

import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ClosableDataSource;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.PooledDataSourceFactory;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ClassLoaderResourceAccessor;
import net.sourceforge.argparse4j.inf.Namespace;

import java.sql.Connection;

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
        final ClassLoaderResourceAccessor accessor = new ClassLoaderResourceAccessor();
        final DatabaseConfiguration dbConfig = strategy.getDatabaseConfiguration(configuration);
        dbConfig.setMaxSize(1);
        dbConfig.setMinSize(1);

        final PooledDataSourceFactory factory = new PooledDataSourceFactory(dbConfig);
        final ClosableDataSource dataSource = factory.build();
        try {
            final Connection connection = dataSource.getConnection();
            try {
                final DatabaseConnection conn = new JdbcConnection(connection);
                final Liquibase liquibase = new Liquibase("migrations.xml", accessor, conn);
                try {
                    liquibase.validate();
                    run(namespace, liquibase);
                } catch (ValidationFailedException e) {
                    e.printDescriptiveError(System.err);
                }
            } finally {
                connection.close();
            }
        } finally {
            dataSource.close();
        }
    }

    public abstract void run(Namespace namespace, Liquibase liquibase) throws Exception;
}
