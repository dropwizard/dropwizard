package io.dropwizard.migrations;

import io.dropwizard.core.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class DbFastForwardCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {

    private PrintStream printStream = System.out;

    protected DbFastForwardCommand(DatabaseConfiguration<T> strategy, Class<T> configurationClass, String migrationsFileName) {
        super("fast-forward",
              "Mark the next pending change set as applied without running it",
              strategy,
              configurationClass,
              migrationsFileName);
    }

    void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-n", "--dry-run")
                 .action(Arguments.storeTrue())
                 .dest("dry-run")
                 .setDefault(Boolean.FALSE)
                 .help("output the DDL to stdout, don't run it");

        subparser.addArgument("-a", "--all")
                 .action(Arguments.storeTrue())
                 .dest("all")
                 .setDefault(Boolean.FALSE)
                 .help("mark all pending change sets as applied");

        subparser.addArgument("-i", "--include")
                 .action(Arguments.append())
                 .dest("contexts")
                 .help("include change sets from the given context");
    }

    @Override
    public void run(Namespace namespace,
                    Liquibase liquibase) throws Exception {
        final String context = getContext(namespace);
        final boolean isDryRun = Boolean.TRUE.equals(namespace.getBoolean("dry-run"));
        if (Boolean.TRUE.equals(namespace.getBoolean("all"))) {
            if (isDryRun) {
                liquibase.changeLogSync(context, new OutputStreamWriter(printStream, StandardCharsets.UTF_8));
            } else {
                liquibase.changeLogSync(context);
            }
        } else {
            if (isDryRun) {
                liquibase.markNextChangeSetRan(context, new OutputStreamWriter(printStream, StandardCharsets.UTF_8));
            } else {
                liquibase.markNextChangeSetRan(context);
            }
        }
    }

    private String getContext(Namespace namespace) {
        final List<Object> contexts = namespace.getList("contexts");
        if (contexts == null) {
            return "";
        }
        return contexts.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }
}
