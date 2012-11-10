package com.yammer.dropwizard.migrations;

import com.google.common.base.Charsets;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStreamWriter;

public class DbStatusCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbStatusCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("status", "Check for pending change sets.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-v", "--verbose")
                 .action(Arguments.storeTrue())
                 .dest("verbose")
                 .help("Output verbose information");
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.reportStatus(namespace.getBoolean("verbose"),
                               null,
                               new OutputStreamWriter(System.out, Charsets.UTF_8));
    }
}
