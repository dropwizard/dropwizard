package io.dropwizard.migrations;

import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.Liquibase;
import liquibase.change.CheckSum;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class DbCalculateChecksumCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger("liquibase");

    private Consumer<CheckSum> checkSumConsumer = (checkSum) -> LOGGER.info("checksum = {}", checkSum);

    public DbCalculateChecksumCommand(DatabaseConfiguration<T> strategy, Class<T> configurationClass, String migrationsFileName) {
        super("calculate-checksum", "Calculates and prints a checksum for a change set", strategy,
            configurationClass, migrationsFileName);
    }

    void setCheckSumConsumer(Consumer<CheckSum> checkSumConsumer) {
        this.checkSumConsumer = checkSumConsumer;
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
        checkSumConsumer.accept(checkSum);
    }
}
