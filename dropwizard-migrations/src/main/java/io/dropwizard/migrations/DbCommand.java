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

    public DbCommand(String name, DatabaseConfiguration<T> strategy, Class<T> configurationClass, String migrationsFileName) {
        super(name, "Run database migration tasks", strategy, configurationClass, migrationsFileName);
        this.subcommands = new TreeMap<>();
        addSubcommand(new DbCalculateChecksumCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbClearChecksumsCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbDropAllCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbDumpCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbFastForwardCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbGenerateDocsCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbLocksCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbMigrateCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbPrepareRollbackCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbRollbackCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbStatusCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbTagCommand<>(strategy, configurationClass, migrationsFileName));
        addSubcommand(new DbTestCommand<>(strategy, configurationClass, migrationsFileName));
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
