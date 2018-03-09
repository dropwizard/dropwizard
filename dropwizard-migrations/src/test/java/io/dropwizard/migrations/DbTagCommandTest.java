package io.dropwizard.migrations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbTagCommandTest extends AbstractMigrationTest {

    private final String migrationsFileName = "migrations-ddl.xml";
    private final DbTagCommand<TestMigrationConfiguration> dbTagCommand = new DbTagCommand<>(
        new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);

    @Test
    public void testRun() throws Exception {
        // Migrate some DDL changes
        final TestMigrationConfiguration conf = createConfiguration(getDatabaseUrl());
        final DbMigrateCommand<TestMigrationConfiguration> dbMigrateCommand = new DbMigrateCommand<>(
            new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
        dbMigrateCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // Tag them
        dbTagCommand.run(null, new Namespace(ImmutableMap.of("tag-name", ImmutableList.of("v1"))), conf);

        // Verify that the tag exists
        try (CloseableLiquibase liquibase = dbTagCommand.openLiquibase(conf.getDataSource(),
            new Namespace(ImmutableMap.of()))) {
            assertThat(liquibase.tagExists("v1")).isTrue();
        }
    }

    @Test
    public void testPrintHelp() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        createSubparser(dbTagCommand).printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8)).isEqualTo(String.format(
            "usage: db tag [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]%n" +
            "          [--schema SCHEMA] [file] tag-name%n" +
            "%n" +
            "Tag the database schema.%n" +
            "%n" +
            "positional arguments:%n" +
            "  file                   application configuration file%n" +
            "  tag-name               The tag name%n" +
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
