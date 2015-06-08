package io.dropwizard.migrations;

import io.dropwizard.db.DataSourceFactory;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.File;
import java.io.IOException;

public class AbstractMigrationTest {

    static {
        ArgumentParsers.setTerminalWidthDetection(false);
    }

    protected static Subparser createSubparser(AbstractLiquibaseCommand<?> command) {
        final Subparser subparser = ArgumentParsers.newArgumentParser("db")
                .addSubparsers()
                .addParser(command.getName())
                .description(command.getDescription());
        command.configure(subparser);
        return subparser;
    }

    protected static TestMigrationConfiguration createConfiguration(String databaseUrl) {
        final DataSourceFactory dataSource = new DataSourceFactory();
        dataSource.setDriverClass("org.h2.Driver");
        dataSource.setUser("sa");
        dataSource.setUrl(databaseUrl);
        return new TestMigrationConfiguration(dataSource);
    }

    protected static String createTempFile() {
        try {
            return File.createTempFile("test-example", null).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
