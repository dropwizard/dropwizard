package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import com.yammer.dropwizard.logging.Log;
import liquibase.Liquibase;
import liquibase.change.CheckSum;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class DbCalculateChecksumCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    private static final Log LOG = Log.named("liquibase");

    public DbCalculateChecksumCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("calculate-checksum", "Calculates and prints a checksum for a change set", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("id").nargs(1).help("change set id");
        subparser.addArgument("author").nargs(1).help("author name");
    }

    @Override
    public void run(Namespace namespace,
                    Liquibase liquibase) throws Exception {
        final CheckSum checkSum = liquibase.calculateCheckSum("migrations.xml",
                                                              namespace.<String>getList("id").get(0),
                                                              namespace.<String>getList("author").get(0));
        LOG.info("checksum = {}", checkSum);
    }
}
