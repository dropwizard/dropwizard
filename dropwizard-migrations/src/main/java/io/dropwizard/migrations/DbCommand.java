package io.dropwizard.migrations;

import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.SortedMap;
import java.util.TreeMap;

public class DbCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    private static final String COMMAND_NAME_ATTR = "subcommand";
    private final SortedMap<String, AbstractLiquibaseCommand<T>> subcommands;

    public DbCommand(String name, DatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super(name, "Run database migration tasks", strategy, configurationClass);
        this.subcommands = new TreeMap<>();
        addSubcommand(new DbCalculateChecksumCommand<>(strategy, configurationClass));
        addSubcommand(new DbClearChecksumsCommand<>(strategy, configurationClass));
        addSubcommand(new DbDropAllCommand<>(strategy, configurationClass));
        addSubcommand(new DbDumpCommand<>(strategy, configurationClass));
        addSubcommand(new DbFastForwardCommand<>(strategy, configurationClass));
        addSubcommand(new DbGenerateDocsCommand<>(strategy, configurationClass));
        addSubcommand(new DbLocksCommand<>(strategy, configurationClass));
        addSubcommand(new DbMigrateCommand<>(strategy, configurationClass));
        addSubcommand(new DbPrepareRollbackCommand<>(strategy, configurationClass));
        addSubcommand(new DbRollbackCommand<>(strategy, configurationClass));
        addSubcommand(new DbStatusCommand<>(strategy, configurationClass));
        addSubcommand(new DbTagCommand<>(strategy, configurationClass));
        addSubcommand(new DbTestCommand<>(strategy, configurationClass));
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
