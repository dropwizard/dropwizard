package io.dropwizard.migrations;

import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
class DbTagCommandTest {

    private final String migrationsFileName = "migrations-ddl.xml";
    private final DbTagCommand<TestMigrationConfiguration> dbTagCommand = new DbTagCommand<>(
        new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);

    @Test
    void testRun() throws Exception {
        // Migrate some DDL changes
        final TestMigrationConfiguration conf = MigrationTestSupport.createConfiguration();
        final DbMigrateCommand<TestMigrationConfiguration> dbMigrateCommand = new DbMigrateCommand<>(
            new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, migrationsFileName);
        dbMigrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);

        // Tag them
        dbTagCommand.run(null, new Namespace(Collections.singletonMap("tag-name", Collections.singletonList("v1"))), conf);

        // Verify that the tag exists
        try (CloseableLiquibase liquibase = dbTagCommand.openLiquibase(conf.getDataSource(),
            new Namespace(Collections.emptyMap()))) {
            assertThat(liquibase.tagExists("v1")).isTrue();
        }
    }

    @Test
    void testPrintHelp() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MigrationTestSupport.createSubparser(dbTagCommand).printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8.name())).isEqualToNormalizingNewlines(
            "usage: db tag [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]\n" +
            "          [--schema SCHEMA] [file] tag-name\n" +
            "\n" +
            "Tag the database schema.\n" +
            "\n" +
            "positional arguments:\n" +
            "  file                   application configuration file\n" +
            "  tag-name               The tag name\n" +
            "\n" +
            "named arguments:\n" +
            "  -h, --help             show this help message and exit\n" +
            "  --migrations MIGRATIONS-FILE\n" +
            "                         the file containing  the  Liquibase migrations for\n" +
            "                         the application\n" +
            "  --catalog CATALOG      Specify  the   database   catalog   (use  database\n" +
            "                         default if omitted)\n" +
            "  --schema SCHEMA        Specify the database schema  (use database default\n" +
            "                         if omitted)\n");
    }
}
