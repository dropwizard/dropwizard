package io.dropwizard.migrations;

import io.dropwizard.util.Maps;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
class DbRollbackCommandTest {

    private final String migrationsFileName = "migrations-ddl.xml";
    private final DbRollbackCommand<TestMigrationConfiguration> rollbackCommand = new DbRollbackCommand<>(
        new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
    private final DbMigrateCommand<TestMigrationConfiguration> migrateCommand = new DbMigrateCommand<>(
        new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private TestMigrationConfiguration conf;
    private Jdbi dbi;

    @BeforeEach
    void setUp() {
        final String databaseUrl = MigrationTestSupport.getDatabaseUrl();
        conf = MigrationTestSupport.createConfiguration(databaseUrl);
        dbi = Jdbi.create(databaseUrl, "sa", "");
    }

    @Test
    void testRollbackNChanges() throws Exception {
        // Migrate some DDL changes to the database
        migrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);

        try (Handle handle = dbi.open()) {
            assertThat(MigrationTestSupport.columnExists(handle, "PERSONS", "EMAIL"))
                .isTrue();
        }

        // Rollback the last one (the email field)
        rollbackCommand.run(null, new Namespace(Collections.singletonMap("count", 1)), conf);

        try (Handle handle = dbi.open()) {
            assertThat(MigrationTestSupport.columnExists(handle, "PERSONS", "EMAIL"))
                .isFalse();
        }
    }

    @Test
    void testRollbackNChangesAsDryRun() throws Exception {
        // Migrate some DDL changes to the database
        migrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);

        // Print out the change that rollbacks the second change
        rollbackCommand.setOutputStream(new PrintStream(baos, true));
        rollbackCommand.run(null, new Namespace(Maps.of("count", 1, "dry-run", true)), conf);
        assertThat(baos.toString(UTF_8.name()))
            .containsIgnoringCase("ALTER TABLE PUBLIC.persons DROP COLUMN email;");
    }

    @Test
    void testRollbackToDate() throws Exception {
        // Migrate some DDL changes to the database
        long migrationDate = System.currentTimeMillis();
        migrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);

        try (Handle handle = dbi.open()) {
            assertThat(MigrationTestSupport.tableExists(handle, "PERSONS"))
                .isTrue();
        }

        // Rollback both changes (they're tearDown the migration date)
        rollbackCommand.run(null, new Namespace(Collections.singletonMap("date", new Date(migrationDate - 1000))),
            conf);

        try (Handle handle = dbi.open()) {
            assertThat(MigrationTestSupport.tableExists(handle, "PERSONS"))
                .isFalse();
        }
    }

    @Test
    void testRollbackToDateAsDryRun() throws Exception {
        // Migrate some DDL changes to the database
        long migrationDate = System.currentTimeMillis();
        migrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);

        // Print out a rollback script for both changes tearDown the migration date
        rollbackCommand.setOutputStream(new PrintStream(baos, true));
        rollbackCommand.run(null, new Namespace(Maps.of(
                "date", new Date(migrationDate - 1000),
                "dry-run", true)),
                conf);
        assertThat(baos.toString(UTF_8.name()))
            .containsIgnoringCase("ALTER TABLE PUBLIC.persons DROP COLUMN email;")
            .containsIgnoringCase("DROP TABLE PUBLIC.persons;");
    }

    @Test
    void testRollbackToTag() throws Exception {
        // Migrate the first DDL change to the database
        migrateCommand.run(null, new Namespace(Collections.singletonMap("count", 1)), conf);

        // Tag it
        final DbTagCommand<TestMigrationConfiguration> tagCommand = new DbTagCommand<>(
            new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
        tagCommand.run(null, new Namespace(Collections.singletonMap("tag-name", Collections.singletonList("v1"))), conf);

        try (Handle handle = dbi.open()) {
            assertThat(MigrationTestSupport.columnExists(handle, "PERSONS", "EMAIL"))
                .isFalse();
        }

        // Migrate the second change
        migrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);

        try (Handle handle = dbi.open()) {
            assertThat(MigrationTestSupport.columnExists(handle, "PERSONS", "EMAIL"))
                .isTrue();
        }

        // Rollback to the first change
        rollbackCommand.run(null, new Namespace(Collections.singletonMap("tag", "v1")), conf);

        try (Handle handle = dbi.open()) {
            assertThat(MigrationTestSupport.columnExists(handle, "PERSONS", "EMAIL"))
                .isFalse();
        }
    }

    @Test
    void testRollbackToTagAsDryRun() throws Exception {
        // Migrate the first DDL change to the database
        migrateCommand.run(null, new Namespace(Collections.singletonMap("count", 1)), conf);

        // Tag it
        final DbTagCommand<TestMigrationConfiguration> tagCommand = new DbTagCommand<>(
            new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
        tagCommand.run(null, new Namespace(Collections.singletonMap("tag-name", Collections.singletonList("v1"))), conf);

        // Migrate the second change
        migrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);

        // Print out the rollback script for the second change
        rollbackCommand.setOutputStream(new PrintStream(baos, true));
        rollbackCommand.run(null, new Namespace(Maps.of("tag", "v1", "dry-run", true)), conf);
        assertThat(baos.toString(UTF_8.name()))
            .containsIgnoringCase("ALTER TABLE PUBLIC.persons DROP COLUMN email;");
    }

    @Test
    void testPrintHelp() throws Exception {
        MigrationTestSupport.createSubparser(rollbackCommand).printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8.name())).isEqualTo(String.format(
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
