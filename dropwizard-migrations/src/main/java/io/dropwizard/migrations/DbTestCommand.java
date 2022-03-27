package io.dropwizard.migrations;

import io.dropwizard.core.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.List;
import java.util.stream.Collectors;

public class DbTestCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    public DbTestCommand(DatabaseConfiguration<T> strategy, Class<T> configurationClass, String migrationsFileName) {
        super("test", "Apply and rollback pending change sets.", strategy, configurationClass, migrationsFileName);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-i", "--include")
                 .action(Arguments.append())
                 .dest("contexts")
                 .help("include change sets from the given context");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.updateTestingRollback(getContext(namespace));
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
