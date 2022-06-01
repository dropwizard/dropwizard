package io.dropwizard.migrations;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

@Execution(SAME_THREAD)
class DbPrepareRollbackCommandTest {

    private final DbPrepareRollbackCommand<TestMigrationConfiguration> prepareRollbackCommand =
            new DbPrepareRollbackCommand<>(
                    new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, "migrations-ddl.xml");
    private TestMigrationConfiguration conf;

    @BeforeEach
    void setUp() {
        final String databaseUrl = MigrationTestSupport.getDatabaseUrl();
        conf = MigrationTestSupport.createConfiguration(databaseUrl);
    }

    @Test
    void testRun() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        prepareRollbackCommand.setOutputStream(new PrintStream(baos));
        prepareRollbackCommand.run(null, new Namespace(Collections.emptyMap()), conf);
        assertThat(baos.toString(UTF_8.name()))
                .contains("ALTER TABLE PUBLIC.persons DROP COLUMN email;")
                .contains("DROP TABLE PUBLIC.persons;");
    }

    @Test
    void testPrepareOnlyChange() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        prepareRollbackCommand.setOutputStream(new PrintStream(baos));
        prepareRollbackCommand.run(null, new Namespace(Collections.singletonMap("count", 1)), conf);
        assertThat(baos.toString(UTF_8.name())).contains("DROP TABLE PUBLIC.persons;");
    }

    @Test
    void testPrintHelp() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        MigrationTestSupport.createSubparser(prepareRollbackCommand)
                .printHelp(new PrintWriter(new OutputStreamWriter(out, UTF_8), true));
        assertThat(out.toString(UTF_8.name()))
                .isEqualToNormalizingNewlines("usage: db prepare-rollback [-h] [--migrations MIGRATIONS-FILE]\n"
                        + "          [--catalog CATALOG] [--schema SCHEMA] [-c COUNT] [-i CONTEXTS]\n"
                        + "          [file]\n"
                        + "\n"
                        + "Generate rollback DDL scripts for pending change sets.\n"
                        + "\n"
                        + "positional arguments:\n"
                        + "  file                   application configuration file\n"
                        + "\n"
                        + "named arguments:\n"
                        + "  -h, --help             show this help message and exit\n"
                        + "  --migrations MIGRATIONS-FILE\n"
                        + "                         the file containing  the  Liquibase migrations for\n"
                        + "                         the application\n"
                        + "  --catalog CATALOG      Specify  the   database   catalog   (use  database\n"
                        + "                         default if omitted)\n"
                        + "  --schema SCHEMA        Specify the database schema  (use database default\n"
                        + "                         if omitted)\n"
                        + "  -c COUNT, --count COUNT\n"
                        + "                         limit script to  the  specified  number of pending\n"
                        + "                         change sets\n"
                        + "  -i CONTEXTS, --include CONTEXTS\n"
                        + "                         include change sets from the given context\n");
    }
}
