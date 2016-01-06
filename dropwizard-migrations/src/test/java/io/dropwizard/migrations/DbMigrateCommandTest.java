package io.dropwizard.migrations;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbMigrateCommandTest extends AbstractMigrationTest {

    private DbMigrateCommand<TestMigrationConfiguration> migrateCommand = new DbMigrateCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class);
    private TestMigrationConfiguration conf;
    private String databaseUrl;

    @Before
    public void setUp() throws Exception {
        databaseUrl = "jdbc:h2:" + createTempFile();
        conf = createConfiguration(databaseUrl);
    }

    @Test
    public void testRun() throws Exception {
        migrateCommand.run(null, new Namespace(ImmutableMap.<String, Object>of()), conf);
        try (Handle handle = new DBI(databaseUrl, "sa", "").open()) {
            final List<Map<String, Object>> rows = handle.select("select * from persons");
            assertThat(rows).hasSize(1);
            assertThat(rows.get(0)).isEqualTo(
                    ImmutableMap.of("id", 1, "name", "Bill Smith", "email", "bill@smith.me"));
        }
    }

    @Test
    public void testRunFirstTwoMigration() throws Exception {
        migrateCommand.run(null, new Namespace(ImmutableMap.of("count", (Object) 2)), conf);
        try (Handle handle = new DBI(databaseUrl, "sa", "").open()) {
            assertThat(handle.select("select * from persons")).isEmpty();
        }
    }

    @Test
    public void testDryRun() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        migrateCommand.setOutputStream(new PrintStream(baos));
        migrateCommand.run(null, new Namespace(ImmutableMap.of("dry-run", (Object) true)), conf);
        assertThat(baos.toString("UTF-8")).startsWith(String.format(
                "-- *********************************************************************%n" +
                "-- Update Database Script%n" +
                "-- *********************************************************************%n"));
    }

    @Test
    public void testPrintHelp() throws Exception {
        final Subparser subparser = ArgumentParsers.newArgumentParser("db")
                .addSubparsers()
                .addParser(migrateCommand.getName())
                .description(migrateCommand.getDescription());
        migrateCommand.configure(subparser);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        subparser.printHelp(new PrintWriter(baos, true));

        assertThat(baos.toString("UTF-8")).isEqualTo(String.format(
                        "usage: db migrate [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]%n" +
                        "          [--schema SCHEMA] [-n] [-c COUNT] [-i CONTEXTS] [file]%n" +
                        "%n" +
                        "Apply all pending change sets.%n" +
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
                        "  -c COUNT, --count COUNT%n" +
                        "                         only apply the next N change sets%n" +
                        "  -i CONTEXTS, --include CONTEXTS%n" +
                        "                         include change sets from the given context%n"));
    }
}
