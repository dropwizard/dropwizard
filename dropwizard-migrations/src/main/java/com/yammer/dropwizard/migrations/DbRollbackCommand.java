package com.yammer.dropwizard.migrations;

import com.google.common.base.Charsets;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStreamWriter;
import java.util.Date;

public class DbRollbackCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbRollbackCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("rollback",
              "Rollback the database schema to a previous version.",
              strategy,
              configurationClass);
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
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final String tag = namespace.getString("tag");
        final Integer count = namespace.getInt("count");
        final Date date = (Date) namespace.get("date");
        final Boolean dryRun = namespace.getBoolean("dry-run");

        if (((count == null) && (tag == null) && (date == null)) ||
                (((count != null) && (tag != null)) ||
                        ((count != null) && (date != null)) ||
                        ((tag != null) && (date != null)))) {
            throw new IllegalArgumentException("Must specify either a count, a tag, or a date.");
        }

        if (count != null) {
            if (dryRun) {
                liquibase.rollback(count, "", new OutputStreamWriter(System.out, Charsets.UTF_8));
            } else {
                liquibase.rollback(count, "");
            }
        } else if (tag != null) {
            if (dryRun) {
                liquibase.rollback(tag, null, new OutputStreamWriter(System.out, Charsets.UTF_8));
            } else {
                liquibase.rollback(tag, "");
            }
        } else {
            if (dryRun) {
                liquibase.rollback(date, null, new OutputStreamWriter(System.out, Charsets.UTF_8));
            } else {
                liquibase.rollback(date, "");
            }
        }
    }
}
