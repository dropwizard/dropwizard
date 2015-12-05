package io.dropwizard.migrations;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DbStatusCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {

    private PrintStream outputStream = System.out;

    @VisibleForTesting
    void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    public DbStatusCommand(DatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("status", "Check for pending change sets.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-v", "--verbose")
                 .action(Arguments.storeTrue())
                 .dest("verbose")
                 .help("Output verbose information");
        subparser.addArgument("-i", "--include")
                 .action(Arguments.append())
                 .dest("contexts")
                 .help("include change sets from the given context");
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.reportStatus(MoreObjects.firstNonNull(namespace.getBoolean("verbose"), false),
                               getContext(namespace),
                               new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
    }

    private String getContext(Namespace namespace) {
        final List<Object> contexts = namespace.getList("contexts");
        if (contexts == null) {
            return "";
        }
        return Joiner.on(',').join(contexts);
    }
}
