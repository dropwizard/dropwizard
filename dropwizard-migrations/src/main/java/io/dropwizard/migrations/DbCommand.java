package io.dropwizard.migrations;

import io.dropwizard.core.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import liquibase.Scope;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;

public class DbCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    private static final String COMMAND_NAME_ATTR = "subcommand";
    private final SortedMap<String, AbstractLiquibaseCommand<T>> subcommands;
    @Nullable
    private final Map<String, Object> scopedObjects;

    public DbCommand(String name, DatabaseConfiguration<T> strategy, Class<T> configurationClass, String migrationsFileName) {
        this(name, strategy, configurationClass, migrationsFileName, null);
    }

    public DbCommand(String name, DatabaseConfiguration<T> strategy, Class<T> configurationClass, String migrationsFileName, @Nullable Map<String, Object> scopedObjects) {
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
        this.scopedObjects = scopedObjects;
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
        String scopeId = null;
        if (scopedObjects != null) {
            scopeId = Scope.enter(scopedObjects);
        }
        final AbstractLiquibaseCommand<T> subcommand =
            requireNonNull(subcommands.get(namespace.getString(COMMAND_NAME_ATTR)), "Unable find the command");
        try {
            subcommand.run(namespace, liquibase);
        } finally {
            if (scopeId != null) {
                Scope.exit(scopeId);
            }
        }
    }
}
