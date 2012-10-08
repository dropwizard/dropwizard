package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class DbTagCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbTagCommand(ConfigurationStrategy<T> strategy, Class<T> configurationClass) {
        super("tag", "Tag the database schema.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("tag-name").nargs(1).required(true).help("The tag name");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.tag(namespace.<String>getList("tag-name").get(0));
    }
}
