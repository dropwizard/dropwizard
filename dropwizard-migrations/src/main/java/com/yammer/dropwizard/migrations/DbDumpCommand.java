package com.yammer.dropwizard.migrations;

import com.google.common.base.Charsets;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import liquibase.Liquibase;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.diff.DiffStatusListener;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.PrintStream;

public class DbDumpCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbDumpCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("dump",
              "Generate a dump of the existing database state.",
              strategy,
              configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-o", "--output")
                 .dest("output")
                 .help("Write output to <file> instead of stdout");

        final ArgumentGroup tables = subparser.addArgumentGroup("Tables");
        tables.addArgument("--tables")
              .action(Arguments.storeTrue())
              .dest("tables")
              .help("Check for added or removed tables (default)");
        tables.addArgument("--ignore-tables")
              .action(Arguments.storeFalse())
              .dest("tables")
              .help("Ignore tables");

        final ArgumentGroup columns = subparser.addArgumentGroup("Columns");
        columns.addArgument("--columns")
               .action(Arguments.storeTrue())
               .dest("columns")
               .help("Check for added, removed, or modified tables (default)");
        columns.addArgument("--ignore-columns")
               .action(Arguments.storeFalse())
               .dest("columns")
               .help("Ignore columns");

        final ArgumentGroup views = subparser.addArgumentGroup("Views");
        views.addArgument("--views")
             .action(Arguments.storeTrue())
             .dest("views")
             .help("Check for added, removed, or modified views (default)");
        views.addArgument("--ignore-views")
             .action(Arguments.storeFalse())
             .dest("views")
             .help("Ignore views");

        final ArgumentGroup primaryKeys = subparser.addArgumentGroup("Primary Keys");
        primaryKeys.addArgument("--primary-keys")
                   .action(Arguments.storeTrue())
                   .dest("primary-keys")
                   .help("Check for changed primary keys (default)");
        primaryKeys.addArgument("--ignore-primary-keys")
                   .action(Arguments.storeFalse())
                   .dest("primary-keys")
                   .help("Ignore primary keys");

        final ArgumentGroup uniqueConstraints = subparser.addArgumentGroup("Unique Constraints");
        uniqueConstraints.addArgument("--unique-constraints")
                         .action(Arguments.storeTrue())
                         .dest("unique-constraints")
                         .help("Check for changed unique constraints (default)");
        uniqueConstraints.addArgument("--ignore-unique-constraints")
                         .action(Arguments.storeFalse())
                         .dest("unique-constraints")
                         .help("Ignore unique constraints");

        final ArgumentGroup indexes = subparser.addArgumentGroup("Indexes");
        indexes.addArgument("--indexes")
               .action(Arguments.storeTrue())
               .dest("indexes")
               .help("Check for changed indexes (default)");
        indexes.addArgument("--ignore-indexes")
               .action(Arguments.storeFalse())
               .dest("indexes")
               .help("Ignore indexes");

        final ArgumentGroup foreignKeys = subparser.addArgumentGroup("Foreign Keys");
        foreignKeys.addArgument("--foreign-keys")
                   .action(Arguments.storeTrue())
                   .dest("foreign-keys")
                   .help("Check for changed foreign keys (default)");
        foreignKeys.addArgument("--ignore-foreign-keys")
                   .action(Arguments.storeFalse())
                   .dest("foreign-keys")
                   .help("Ignore foreign keys");

        final ArgumentGroup sequences = subparser.addArgumentGroup("Sequences");
        sequences.addArgument("--sequences")
                 .action(Arguments.storeTrue())
                 .dest("sequences")
                 .help("Check for changed sequences (default)");
        sequences.addArgument("--ignore-sequences")
                 .action(Arguments.storeFalse())
                 .dest("sequences")
                 .help("Ignore foreign keys");

        final ArgumentGroup data = subparser.addArgumentGroup("Data");
        data.addArgument("--data")
            .action(Arguments.storeTrue())
            .dest("data")
            .help("Check for changed data")
            .setDefault(Boolean.FALSE);
        data.addArgument("--ignore-data")
            .action(Arguments.storeFalse())
            .dest("data")
            .help("Ignore data (default)")
            .setDefault(Boolean.FALSE);
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final Diff diff = new Diff(liquibase.getDatabase(), (String) null);
        diff.addStatusListener(new DiffStatusListener() {
            @Override
            public void statusUpdate(String message) {
                System.err.println(message);
            }
        });
        diff.setDiffColumns(namespace.getBoolean("columns"));
        diff.setDiffData(namespace.getBoolean("data"));
        diff.setDiffForeignKeys(namespace.getBoolean("foreign-keys"));
        diff.setDiffIndexes(namespace.getBoolean("indexes"));
        diff.setDiffPrimaryKeys(namespace.getBoolean("primary-keys"));
        diff.setDiffSequences(namespace.getBoolean("sequences"));
        diff.setDiffTables(namespace.getBoolean("tables"));
        diff.setDiffUniqueConstraints(namespace.getBoolean("unique-constraints"));
        diff.setDiffViews(namespace.getBoolean("views"));

        final DiffResult diffResult = diff.compare();
        final String filename = namespace.getString("output");
        if (filename != null) {
            final PrintStream file = new PrintStream(filename, Charsets.UTF_8.name());
            try {
                diffResult.printChangeLog(file, liquibase.getDatabase());
            } finally {
                file.close();
            }
        } else {
            diffResult.printChangeLog(System.out, liquibase.getDatabase());
        }
    }
}
