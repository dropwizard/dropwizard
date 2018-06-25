package io.dropwizard.migrations;

import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DbRollbackCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {

    private PrintStream outputStream = System.out;

    public DbRollbackCommand(DatabaseConfiguration<T> strategy, Class<T> configurationClass, String migrationsFileName) {
        super("rollback",
            "Rollback the database schema to a previous version.",
            strategy,
            configurationClass,
            migrationsFileName);
    }

    void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-n", "--dry-run")
                 .action(Arguments.storeTrue())
                 .dest("dry-run")
                 .setDefault(Boolean.FALSE)
                 .help("Output the DDL to stdout, don't run it");
        subparser.addArgument("-t", "--tag").dest("tag").help("Rollback to the given tag");
        subparser.addArgument("-d", "--date")
                 .dest("date")
                 .type(Date.class)
                 .help("Rollback to the given date");
        subparser.addArgument("-c", "--count")
                 .dest("count")
                 .type(Integer.class)
                 .help("Rollback the specified number of change sets");
        subparser.addArgument("-i", "--include")
                 .action(Arguments.append())
                 .dest("contexts")
                 .help("include change sets from the given context");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final String tag = namespace.getString("tag");
        final Integer count = namespace.getInt("count");
        final Date date = namespace.get("date");
        final boolean dryRun = namespace.getBoolean("dry-run") == null ? false : namespace.getBoolean("dry-run");
        final String context = getContext(namespace);
        if (Stream.of(tag, count, date).filter(Objects::nonNull).count() != 1) {
            throw new IllegalArgumentException("Must specify either a count, a tag, or a date.");
        }

        if (count != null) {
            if (dryRun) {
                liquibase.rollback(count, context, new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            } else {
                liquibase.rollback(count, context);
            }
        } else if (tag != null) {
            if (dryRun) {
                liquibase.rollback(tag, context, new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            } else {
                liquibase.rollback(tag, context);
            }
        } else {
            if (dryRun) {
                liquibase.rollback(date, context, new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            } else {
                liquibase.rollback(date, context);
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
