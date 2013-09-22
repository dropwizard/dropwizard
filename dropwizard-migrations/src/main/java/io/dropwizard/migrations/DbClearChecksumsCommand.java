package io.dropwizard.migrations;

import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;

public class DbClearChecksumsCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbClearChecksumsCommand(DatabaseConfiguration<T> strategy, Class<T> configurationClass) {
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
