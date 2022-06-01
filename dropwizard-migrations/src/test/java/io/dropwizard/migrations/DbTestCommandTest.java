package io.dropwizard.migrations;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

@Execution(SAME_THREAD)
class DbTestCommandTest {

    private final DbTestCommand<TestMigrationConfiguration> dbTestCommand = new DbTestCommand<>(
            new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, "migrations-ddl.xml");

    @Test
    void testRun() throws Exception {
        // Apply and rollback some DDL changes
        final TestMigrationConfiguration conf = MigrationTestSupport.createConfiguration();
        assertThatNoException().isThrownBy(() -> dbTestCommand.run(null, new Namespace(Collections.emptyMap()), conf));
    }

    @Test
    void testPrintHelp() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MigrationTestSupport.createSubparser(dbTestCommand)
                .printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8.name()))
                .isEqualToNormalizingNewlines("usage: db test [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]\n"
                        + "          [--schema SCHEMA] [-i CONTEXTS] [file]\n"
                        + "\n"
                        + "Apply and rollback pending change sets.\n"
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
                        + "  -i CONTEXTS, --include CONTEXTS\n"
                        + "                         include change sets from the given context\n");
    }
}
