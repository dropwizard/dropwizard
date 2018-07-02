package io.dropwizard.migrations;

import io.dropwizard.util.Maps;
import liquibase.change.CheckSum;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbCalculateChecksumCommandTest extends AbstractMigrationTest {

    private DbCalculateChecksumCommand<TestMigrationConfiguration> migrateCommand = new DbCalculateChecksumCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");

    @Test
    public void testRun() throws Exception {
        final AtomicBoolean checkSumVerified = new AtomicBoolean();
        migrateCommand.setCheckSumConsumer(checkSum -> {
            assertThat(checkSum).isEqualTo(CheckSum.parse("8:0f3683b37321ccfb1694a044986de4d9"));
            checkSumVerified.set(true);
        });
        migrateCommand.run(null, new Namespace(Maps.of(
                "id", Collections.singletonList("2"),
                "author", Collections.singletonList("db_dev"))),
                createConfiguration(getDatabaseUrl()));
        assertThat(checkSumVerified.get()).isTrue();
    }

    @Test
    public void testHelpPage() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        createSubparser(migrateCommand).printHelp(new PrintWriter(new OutputStreamWriter(out, UTF_8), true));
        assertThat(out.toString(UTF_8)).isEqualTo(String.format(
            "usage: db calculate-checksum [-h] [--migrations MIGRATIONS-FILE]%n" +
                "          [--catalog CATALOG] [--schema SCHEMA] [file] id author%n" +
                "%n" +
                "Calculates and prints a checksum for a change set%n" +
                "%n" +
                "positional arguments:%n" +
                "  file                   application configuration file%n" +
                "  id                     change set id%n" +
                "  author                 author name%n" +
                "%n" +
                "named arguments:%n" +
                "  -h, --help             show this help message and exit%n" +
                "  --migrations MIGRATIONS-FILE%n" +
                "                         the file containing  the  Liquibase migrations for%n" +
                "                         the application%n" +
                "  --catalog CATALOG      Specify  the   database   catalog   (use  database%n" +
                "                         default if omitted)%n" +
                "  --schema SCHEMA        Specify the database schema  (use database default%n" +
                "                         if omitted)%n"));
    }
}
