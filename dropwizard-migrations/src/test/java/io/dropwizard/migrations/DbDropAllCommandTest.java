package io.dropwizard.migrations;

import com.google.common.collect.ImmutableMap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import net.jcip.annotations.NotThreadSafe;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbDropAllCommandTest extends AbstractMigrationTest {

    private DbDropAllCommand<TestMigrationConfiguration> dropAllCommand = new DbDropAllCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");

    @Test
    public void testRun() throws Exception {
        final String databaseUrl = getDatabaseUrl();
        final TestMigrationConfiguration conf = createConfiguration(databaseUrl);

        // Create some data
        new DbMigrateCommand<>(
            TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml")
            .run(null, new Namespace(ImmutableMap.of()), conf);

        // Drop it
        dropAllCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // After we dropped data and schema, we should be able to create the "persons" table again
        try (Handle handle = new DBI(databaseUrl, "sa", "").open()) {
            handle.execute("create table persons(id int, name varchar(255))");
        }
    }

    @Test
    public void testHelpPage() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        createSubparser(dropAllCommand).printHelp(new PrintWriter(new OutputStreamWriter(out, UTF_8), true));
        assertThat(out.toString(UTF_8)).isEqualTo(String.format(
            "usage: db drop-all [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]%n" +
                "          [--schema SCHEMA] --confirm-delete-everything [file]%n" +
                "%n" +
                "Delete all user-owned objects from the database.%n" +
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
                "  --confirm-delete-everything%n" +
                "                         indicate you  understand  this  deletes everything%n" +
                "                         in your database%n"));
    }
}
