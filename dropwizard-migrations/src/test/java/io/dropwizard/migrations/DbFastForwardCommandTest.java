package io.dropwizard.migrations;

import net.sourceforge.argparse4j.inf.Namespace;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
class DbFastForwardCommandTest {
    private final DbFastForwardCommand<TestMigrationConfiguration> fastForwardCommand = new DbFastForwardCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");
    private TestMigrationConfiguration conf;

    private Jdbi dbi;

    @BeforeEach
    void setUp() {
        final String databaseUrl = MigrationTestSupport.getDatabaseUrl();
        conf = MigrationTestSupport.createConfiguration(databaseUrl);
        dbi = Jdbi.create(databaseUrl, "sa", "");
    }

    @Test
    void testFastForwardFirst() throws Exception {
        // Create the "persons" table manually
        try (Handle handle = dbi.open()) {
            handle.execute("create table persons(id int, name varchar(255))");
        }

        // Fast-forward one change
        fastForwardCommand.run(null, new Namespace(Map.of("all", false, "dry-run", false)), conf);

        // 2nd and 3rd migrations is performed
        new DbMigrateCommand<>(
            TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml")
            .run(null, new Namespace(Map.of()), conf);

        // 1 entry has been added to the persons table
        try (Handle handle = dbi.open()) {
            assertThat(handle.createQuery("select count(*) from persons")
                .mapTo(Integer.class)
                .first())
                .isEqualTo(1);
        }
    }

    @Test
    void testFastForwardAll() throws Exception {
        // Create the "persons" table manually and add some data
        try (Handle handle = dbi.open()) {
            handle.execute("create table persons(id int, name varchar(255))");
            handle.execute("insert into persons (id, name) values (12, 'Greg Young')");
        }

        // Fast-forward all the changes
        fastForwardCommand.run(null, new Namespace(Map.of("all", true, "dry-run", false)), conf);

        // No migrations are performed
        new DbMigrateCommand<>(
            TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml")
            .run(null, new Namespace(Map.of()), conf);

        // Nothing is added to the persons table
        try (Handle handle = dbi.open()) {
            assertThat(handle.createQuery("select count(*) from persons")
                .mapTo(Integer.class)
                .first())
                .isEqualTo(1);
        }
    }

    @Test
    void testFastForwardFirstDryRun() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fastForwardCommand.setPrintStream(new PrintStream(baos));

        // Fast-forward one change
        fastForwardCommand.run(null, new Namespace(Map.of("all", false, "dry-run", true)), conf);

        assertThat(baos.toString(UTF_8)
                .lines()
                .filter(s -> s.startsWith("INSERT INTO PUBLIC.DATABASECHANGELOG (")))
            .hasSize(1);
    }

    @Test
    void testFastForwardAllDryRun() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fastForwardCommand.setPrintStream(new PrintStream(baos));

        // Fast-forward 3 changes
        fastForwardCommand.run(null, new Namespace(Map.of("all", true, "dry-run", true)), conf);

        assertThat(baos.toString(UTF_8)
                .lines()
                .filter(s -> s.startsWith("INSERT INTO PUBLIC.DATABASECHANGELOG (")))
            .hasSize(3);
    }

    @Test
    void testPrintHelp() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MigrationTestSupport.createSubparser(fastForwardCommand).printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8.name())).isEqualToNormalizingNewlines(
            "usage: db fast-forward [-h] [--migrations MIGRATIONS-FILE]\n" +
                "          [--catalog CATALOG] [--schema SCHEMA] [-n] [-a] [-i CONTEXTS]\n" +
                "          [file]\n" +
                "\n" +
                "Mark the next pending change set as applied without running it\n" +
                "\n" +
                "positional arguments:\n" +
                "  file                   application configuration file\n" +
                "\n" +
                "named arguments:\n" +
                "  -h, --help             show this help message and exit\n" +
                "  --migrations MIGRATIONS-FILE\n" +
                "                         the file containing  the  Liquibase migrations for\n" +
                "                         the application\n" +
                "  --catalog CATALOG      Specify  the   database   catalog   (use  database\n" +
                "                         default if omitted)\n" +
                "  --schema SCHEMA        Specify the database schema  (use database default\n" +
                "                         if omitted)\n" +
                "  -n, --dry-run          output the DDL to stdout, don't run it\n" +
                "  -a, --all              mark all pending change sets as applied\n" +
                "  -i CONTEXTS, --include CONTEXTS\n" +
                "                         include change sets from the given context\n");

    }
}
