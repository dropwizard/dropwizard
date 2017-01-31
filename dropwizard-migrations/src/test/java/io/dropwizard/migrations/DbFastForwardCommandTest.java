package io.dropwizard.migrations;

import com.google.common.collect.ImmutableMap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class DbFastForwardCommandTest extends AbstractMigrationTest {

    private static final Pattern NEWLINE_PATTERN = Pattern.compile(System.lineSeparator());
    private DbFastForwardCommand<TestMigrationConfiguration> fastForwardCommand = new DbFastForwardCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");
    private TestMigrationConfiguration conf;

    private DBI dbi;

    @Before
    public void setUp() throws Exception {
        final String databaseUrl = "jdbc:h2:" + createTempFile();
        conf = createConfiguration(databaseUrl);
        dbi = new DBI(databaseUrl, "sa", "");
    }

    @Test
    public void testFastForwardFirst() throws Exception {
        // Create the "persons" table manually
        try (Handle handle = dbi.open()) {
            handle.execute("create table persons(id int, name varchar(255))");
        }

        // Fast-forward one change
        fastForwardCommand.run(null, new Namespace(ImmutableMap.of("all", false, "dry-run", false)), conf);

        // 2nd and 3rd migrations is performed
        new DbMigrateCommand<>(
            TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml")
            .run(null, new Namespace(ImmutableMap.of()), conf);

        // 1 entry has been added to the persons table
        try (Handle handle = dbi.open()) {
            assertThat(handle.createQuery("select count(*) from persons")
                .mapTo(Integer.class)
                .first())
                .isEqualTo(1);
        }
    }

    @Test
    public void testFastForwardAll() throws Exception {
        // Create the "persons" table manually and add some data
        try (Handle handle = dbi.open()) {
            handle.execute("create table persons(id int, name varchar(255))");
            handle.execute("insert into persons (id, name) values (12, 'Greg Young')");
        }

        // Fast-forward all the changes
        fastForwardCommand.run(null, new Namespace(ImmutableMap.of("all", true, "dry-run", false)), conf);

        // No migrations is performed
        new DbMigrateCommand<>(
            TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml")
            .run(null, new Namespace(ImmutableMap.of()), conf);

        // Nothing is added to the persons table
        try (Handle handle = dbi.open()) {
            assertThat(handle.createQuery("select count(*) from persons")
                .mapTo(Integer.class)
                .first())
                .isEqualTo(1);
        }
    }

    @Test
    public void testFastForwardFirstDryRun() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fastForwardCommand.setPrintStream(new PrintStream(baos));

        // Fast-forward one change
        fastForwardCommand.run(null, new Namespace(ImmutableMap.of("all", false, "dry-run", true)), conf);

        assertThat(NEWLINE_PATTERN.splitAsStream(baos.toString("UTF-8"))
            .filter(s -> s.startsWith("INSERT INTO PUBLIC.DATABASECHANGELOG (")))
            .hasSize(1);
    }

    @Test
    public void testFastForwardAllDryRun() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fastForwardCommand.setPrintStream(new PrintStream(baos));

        // Fast-forward 3 changes
        fastForwardCommand.run(null, new Namespace(ImmutableMap.of("all", true, "dry-run", true)), conf);

        assertThat(NEWLINE_PATTERN.splitAsStream(baos.toString("UTF-8"))
            .filter(s -> s.startsWith("INSERT INTO PUBLIC.DATABASECHANGELOG (")))
            .hasSize(3);
    }

    @Test
    public void testPrintHelp() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        createSubparser(fastForwardCommand).printHelp(new PrintWriter(baos, true));
        assertThat(baos.toString("UTF-8")).isEqualTo(String.format(
            "usage: db fast-forward [-h] [--migrations MIGRATIONS-FILE]%n" +
            "          [--catalog CATALOG] [--schema SCHEMA] [-n] [-a] [-i CONTEXTS]%n" +
            "          [file]%n" +
            "%n" +
            "Mark the next pending change set as applied without running it%n" +
            "%n" +
            "positional arguments:%n" +
            "  file                   application configuration file%n" +
            "%n" +
            "optional arguments:%n" +
            "  -h, --help             show this help message and exit%n" +
            "  --migrations MIGRATIONS-FILE%n" +
            "                         the file containing  the  Liquibase migrations for%n" +
            "                         the application%n" +
            "  --catalog CATALOG      Specify  the   database   catalog   (use  database%n" +
            "                         default if omitted)%n" +
            "  --schema SCHEMA        Specify the database schema  (use database default%n" +
            "                         if omitted)%n" +
            "  -n, --dry-run          output the DDL to stdout, don't run it%n" +
            "  -a, --all              mark all pending change sets as applied%n" +
            "  -i CONTEXTS, --include CONTEXTS%n" +
            "                         include change sets from the given context%n"));

    }
}
