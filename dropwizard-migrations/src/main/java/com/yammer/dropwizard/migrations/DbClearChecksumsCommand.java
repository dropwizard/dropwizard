package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.MultiDbConfigurationStrategy;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;

public class DbClearChecksumsCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbClearChecksumsCommand(MultiDbConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("clear-checksums",
              "Removes all saved checksums from the database log",
              strategy,
              configurationClass);
    }

    @Override
    public void run(Namespace namespace,
                    Liquibase liquibase) throws Exception {
        liquibase.clearCheckSums();
    }
}
