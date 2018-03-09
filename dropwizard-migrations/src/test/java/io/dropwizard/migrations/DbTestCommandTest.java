package io.dropwizard.migrations;

import com.google.common.collect.ImmutableMap;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbTestCommandTest extends AbstractMigrationTest {

    private final DbTestCommand<TestMigrationConfiguration> dbTestCommand = new DbTestCommand<>(
        new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, "migrations-ddl.xml");

    @Test
    public void testRun() throws Exception {
        // Apply and rollback some DDL changes
        final TestMigrationConfiguration conf = createConfiguration(getDatabaseUrl());
        dbTestCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // No exception, the test passed
    }

    @Test
    public void testPrintHelp() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        createSubparser(dbTestCommand).printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8)).isEqualTo(String.format(
            "usage: db test [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]%n" +
                "          [--schema SCHEMA] [-i CONTEXTS] [file]%n" +
                "%n" +
                "Apply and rollback pending change sets.%n" +
                "%n" +
                "positional arguments:%n" +
                "  file                   application configuration file%n" +
                "%n" +
                "named arguments:%n" +
                "  -h, --help             show this help message and exit%n" +
                "  --migrations MIGRATIONS-FILE%n" +
                "                         the file containing  the  Liquibase migrations for%n" +
                "                         the application%n" +
                "  --catalog CATALOG      Specify  the   database   catalog   (use  database%n" +
                "                         default if omitted)%n" +
                "  --schema SCHEMA        Specify the database schema  (use database default%n" +
                "                         if omitted)%n" +
                "  -i CONTEXTS, --include CONTEXTS%n" +
                "                         include change sets from the given context%n"));
    }
}
