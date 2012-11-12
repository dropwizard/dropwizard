package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;

public class DbTestCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbTestCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("test", "Apply and rollback pending change sets.", strategy, configurationClass);
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.updateTestingRollback(null);
    }
}
