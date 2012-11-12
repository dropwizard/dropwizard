package com.yammer.dropwizard.migrations;

import com.google.common.collect.Maps;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.SortedMap;

public class DbCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    private static final String COMMAND_NAME_ATTR = "subcommand";
    private final SortedMap<String, AbstractLiquibaseCommand<T>> subcommands;

    public DbCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("db", "Run database migration tasks", strategy, configurationClass);
        this.subcommands = Maps.newTreeMap();
        addSubcommand(new DbCalculateChecksumCommand<T>(strategy, configurationClass));
        addSubcommand(new DbClearChecksumsCommand<T>(strategy, configurationClass));
        addSubcommand(new DbDropAllCommand<T>(strategy, configurationClass));
        addSubcommand(new DbDumpCommand<T>(strategy, configurationClass));
        addSubcommand(new DbFastForwardCommand<T>(strategy, configurationClass));
        addSubcommand(new DbGenerateDocsCommand<T>(strategy, configurationClass));
        addSubcommand(new DbLocksCommand<T>(strategy, configurationClass));
        addSubcommand(new DbMigrateCommand<T>(strategy, configurationClass));
        addSubcommand(new DbPrepareRollbackCommand<T>(strategy, configurationClass));
        addSubcommand(new DbRollbackCommand<T>(strategy, configurationClass));
        addSubcommand(new DbStatusCommand<T>(strategy, configurationClass));
        addSubcommand(new DbTagCommand<T>(strategy, configurationClass));
        addSubcommand(new DbTestCommand<T>(strategy, configurationClass));
    }

    private void addSubcommand(AbstractLiquibaseCommand<T> subcommand) {
        subcommands.put(subcommand.getName(), subcommand);
    }

    @Override
    public void configure(Subparser subparser) {
        for (AbstractLiquibaseCommand<T> subcommand : subcommands.values()) {
            final Subparser cmdParser = subparser.addSubparsers()
                                                 .addParser(subcommand.getName())
                                                 .setDefault(COMMAND_NAME_ATTR, subcommand.getName())
                                                 .description(subcommand.getDescription());
            subcommand.configure(cmdParser);
        }
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final AbstractLiquibaseCommand<T> subcommand = subcommands.get(namespace.getString(COMMAND_NAME_ATTR));
        subcommand.run(namespace, liquibase);
    }
}
