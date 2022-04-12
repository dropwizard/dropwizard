package io.dropwizard.migrations;

import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
class DbStatusCommandTest {

    private final DbStatusCommand<TestMigrationConfiguration> statusCommand =
            new DbStatusCommand<>(new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, "migrations.xml");
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private TestMigrationConfiguration conf;

    @BeforeEach
    void setUp() {
        conf = MigrationTestSupport.createConfiguration();

        statusCommand.setOutputStream(new PrintStream(baos));
    }

    @Test
    void testRunOnMigratedDb() throws Exception {
        final URI existedDbPathUri = Objects.requireNonNull(getClass().getResource("/test-db.mv.db")).toURI();
        final String existedDbPath = Paths.get(existedDbPathUri).toString();
        final String existedDbUrl = "jdbc:h2:" + existedDbPath.substring(0, existedDbPath.length() - ".mv.db".length());
        final TestMigrationConfiguration existedDbConf = MigrationTestSupport.createConfiguration(existedDbUrl);

        statusCommand.run(null, new Namespace(Collections.emptyMap()), existedDbConf);
        assertThat(baos.toString(UTF_8.name())).matches("\\S+ is up to date" + System.lineSeparator());
    }

    @Test
    void testRun() throws Exception {
        statusCommand.run(null, new Namespace(Collections.emptyMap()), conf);
        assertThat(baos.toString(UTF_8.name())).matches(
                "3 change sets have not been applied to \\S+" + System.lineSeparator());
    }

    @Test
    void testVerbose() throws Exception {
        statusCommand.run(null, new Namespace(Collections.singletonMap("verbose", true)), conf);
        assertThat(baos.toString(UTF_8.name())).matches(
                "3 change sets have not been applied to \\S+" + System.lineSeparator() +
                        "\\s*migrations\\.xml::1::db_dev"  + System.lineSeparator() +
                        "\\s*migrations\\.xml::2::db_dev"  + System.lineSeparator() +
                        "\\s*migrations\\.xml::3::db_dev" + System.lineSeparator());
    }

    @Test
    void testPrintHelp() throws Exception {
        MigrationTestSupport.createSubparser(statusCommand).printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8.name())).isEqualToNormalizingNewlines(
                "usage: db status [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]\n" +
                        "          [--schema SCHEMA] [-v] [-i CONTEXTS] [file]\n" +
                        "\n" +
                        "Check for pending change sets.\n" +
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
                        "  -v, --verbose          Output verbose information\n" +
                        "  -i CONTEXTS, --include CONTEXTS\n" +
                        "                         include change sets from the given context\n");
    }
}
