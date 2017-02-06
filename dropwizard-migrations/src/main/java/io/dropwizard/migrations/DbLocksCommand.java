package io.dropwizard.migrations;

import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.PrintStream;

import static com.google.common.base.MoreObjects.firstNonNull;

public class DbLocksCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {

    private PrintStream printStream = System.out;

    public DbLocksCommand(DatabaseConfiguration<T> strategy, Class<T> configurationClass, String migrationsFileName) {
        super("locks", "Manage database migration locks", strategy, configurationClass, migrationsFileName);
    }

    @VisibleForTesting
    void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-l", "--list")
                 .dest("list")
                 .action(Arguments.storeTrue())
                 .setDefault(Boolean.FALSE)
                 .help("list all open locks");

        subparser.addArgument("-r", "--force-release")
                 .dest("release")
                 .action(Arguments.storeTrue())
                 .setDefault(Boolean.FALSE)
                 .help("forcibly release all open locks");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final boolean list = firstNonNull(namespace.getBoolean("list"), false);
        final boolean release = firstNonNull(namespace.getBoolean("release"), false);

        if (list == release) {
            throw new IllegalArgumentException("Must specify either --list or --force-release");
        } else if (list) {
            liquibase.reportLocks(printStream);
        } else {
            liquibase.forceReleaseLocks();
        }
    }
}
