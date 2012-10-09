package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class DbDropAllCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbDropAllCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("drop-all", "Delete all user-owned objects from the database.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
        subparser.addArgument("--confirm-delete-everything")
                 .action(Arguments.storeTrue())
                 .required(true)
                 .help("indicate you understand this deletes everything in your database");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.dropAll();
    }
}
