package com.yammer.dropwizard.migrations;

import com.google.common.base.Charsets;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStreamWriter;

public class DbMigrateCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbMigrateCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("migrate", "Apply all pending change sets.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-n", "--dry-run")
                 .action(Arguments.storeTrue())
                 .dest("dry-run")
                 .setDefault(Boolean.FALSE)
                 .help("Output the DDL to stdout, don't run it");
        subparser.addArgument("-c", "--count")
                 .type(Integer.class)
                 .dest("count")
                 .help("Only apply the next N change sets");
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final Integer count = namespace.getInt("count");
        final Boolean dryRun = namespace.getBoolean("dry-run");
        if (count != null) {
            if (dryRun) {
                liquibase.update(count, "", new OutputStreamWriter(System.out, Charsets.UTF_8));
            } else {
                liquibase.update(count, "");
            }
        } else {
            if (dryRun) {
                liquibase.update("", new OutputStreamWriter(System.out, Charsets.UTF_8));
            } else {
                liquibase.update("");
            }
        }
    }
}
