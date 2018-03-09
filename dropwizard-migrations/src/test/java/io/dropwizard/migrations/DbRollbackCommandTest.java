package io.dropwizard.migrations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbRollbackCommandTest extends AbstractMigrationTest {

    private final String migrationsFileName = "migrations-ddl.xml";
    private final DbRollbackCommand<TestMigrationConfiguration> rollbackCommand = new DbRollbackCommand<>(
        new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
    private final DbMigrateCommand<TestMigrationConfiguration> migrateCommand = new DbMigrateCommand<>(
        new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private TestMigrationConfiguration conf;
    private DBI dbi;

    @Before
    public void setUp() throws Exception {
        final String databaseUrl = getDatabaseUrl();
        conf = createConfiguration(databaseUrl);
        dbi = new DBI(databaseUrl, "sa", "");
    }

    @Test
    public void testRollbackNChanges() throws Exception {
        // Migrate some DDL changes to the database
        migrateCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // Rollback the last one (the email field)
        rollbackCommand.run(null, new Namespace(ImmutableMap.of("count", 1)), conf);

        // Now we can add it
        dbi.useHandle(h -> h.execute("alter table persons add column email varchar(128)"));
    }

    @Test
    public void testRollbackNChangesAsDryRun() throws Exception {
        // Migrate some DDL changes to the database
        migrateCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // Print out the change that rollbacks the second change
        rollbackCommand.setOutputStream(new PrintStream(baos, true));
        rollbackCommand.run(null, new Namespace(ImmutableMap.of("count", 1, "dry-run", true)), conf);
        assertThat(baos.toString(UTF_8))
            .containsIgnoringCase("ALTER TABLE PUBLIC.persons DROP COLUMN email;");
    }

    @Test
    public void testRollbackToDate() throws Exception {
        // Migrate some DDL changes to the database
        long migrationDate = System.currentTimeMillis();
        migrateCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // Rollback both changes (they're after the migration date)
        rollbackCommand.run(null, new Namespace(ImmutableMap.of("date", new Date(migrationDate - 1000))),
            conf);

        // Verify we can creat the table
        dbi.useHandle(h -> h.execute("create table persons(id int, name varchar(255))"));
    }

    @Test
    public void testRollbackToDateAsDryRun() throws Exception {
        // Migrate some DDL changes to the database
        long migrationDate = System.currentTimeMillis();
        migrateCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // Print out a rollback script for both changes after the migration date
        rollbackCommand.setOutputStream(new PrintStream(baos, true));
        rollbackCommand.run(null, new Namespace(ImmutableMap.of("date", new Date(migrationDate - 1000),
            "dry-run", true)), conf);
        assertThat(baos.toString(UTF_8))
            .containsIgnoringCase("ALTER TABLE PUBLIC.persons DROP COLUMN email;")
            .containsIgnoringCase("DROP TABLE PUBLIC.persons;");
    }

    @Test
    public void testRollbackToTag() throws Exception {
        // Migrate the first DDL change to the database
        migrateCommand.run(null, new Namespace(ImmutableMap.of("count", 1)), conf);

        // Tag it
        final DbTagCommand<TestMigrationConfiguration> tagCommand = new DbTagCommand<>(
            new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
        tagCommand.run(null, new Namespace(ImmutableMap.of("tag-name", ImmutableList.of("v1"))), conf);

        // Migrate the second change
        migrateCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // Rollback to the first change
        rollbackCommand.run(null, new Namespace(ImmutableMap.of("tag", "v1")), conf);

        // Verify we can add the second change manually
        dbi.useHandle(h -> h.execute("alter table persons add column email varchar(128)"));
    }

    @Test
    public void testRollbackToTagAsDryRun() throws Exception {
        // Migrate the first DDL change to the database
        migrateCommand.run(null, new Namespace(ImmutableMap.of("count", 1)), conf);

        // Tag it
        final DbTagCommand<TestMigrationConfiguration> tagCommand = new DbTagCommand<>(
            new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
        tagCommand.run(null, new Namespace(ImmutableMap.of("tag-name", ImmutableList.of("v1"))), conf);

        // Migrate the second change
        migrateCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // Print out the rollback script for the second change
        rollbackCommand.setOutputStream(new PrintStream(baos, true));
        rollbackCommand.run(null, new Namespace(ImmutableMap.of("tag", "v1", "dry-run", true)), conf);
        assertThat(baos.toString(UTF_8))
            .containsIgnoringCase("ALTER TABLE PUBLIC.persons DROP COLUMN email;");
    }

    @Test
    public void testPrintHelp() throws Exception {
        createSubparser(rollbackCommand).printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8)).isEqualTo(String.format(
            "usage: db rollback [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]%n" +
            "          [--schema SCHEMA] [-n] [-t TAG] [-d DATE] [-c COUNT]%n" +
            "          [-i CONTEXTS] [file]%n" +
            "%n" +
            "Rollback the database schema to a previous version.%n" +
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
            "  -n, --dry-run          Output the DDL to stdout, don't run it%n" +
            "  -t TAG, --tag TAG      Rollback to the given tag%n" +
            "  -d DATE, --date DATE   Rollback to the given date%n" +
            "  -c COUNT, --count COUNT%n" +
            "                         Rollback the specified number of change sets%n" +
            "  -i CONTEXTS, --include CONTEXTS%n" +
            "                         include change sets from the given context%n"));
    }
}
