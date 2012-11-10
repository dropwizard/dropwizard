package com.yammer.dropwizard.migrations;

import com.google.common.base.Charsets;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStreamWriter;

public class DbFastForwardCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    protected DbFastForwardCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("fast-forward",
              "Mark the next pending change set as applied without running it",
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

        subparser.addArgument("-a", "--all")
                 .action(Arguments.storeTrue())
                 .dest("all")
                 .setDefault(Boolean.FALSE)
                 .help("mark all pending change sets as applied");
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void run(Namespace namespace,
                    Liquibase liquibase) throws Exception {
        if (namespace.getBoolean("all")) {
            if (namespace.getBoolean("dry-run")) {
                liquibase.changeLogSync("", new OutputStreamWriter(System.out, Charsets.UTF_8));
            } else {
                liquibase.changeLogSync("");
            }
        } else {
            if (namespace.getBoolean("dry-run")) {
                liquibase.markNextChangeSetRan("", new OutputStreamWriter(System.out, Charsets.UTF_8));
            } else {
                liquibase.markNextChangeSetRan("");
            }
        }
    }
}
