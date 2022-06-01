package io.dropwizard.migrations;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.Mockito;

@Execution(SAME_THREAD)
class DbGenerateDocsCommandTest {

    private final DbGenerateDocsCommand<TestMigrationConfiguration> generateDocsCommand = new DbGenerateDocsCommand<>(
            TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");

    @Test
    void testRun() throws Exception {
        Liquibase liquibase = Mockito.mock(Liquibase.class);
        generateDocsCommand.run(
                new Namespace(Collections.singletonMap("output", Collections.singletonList("/tmp/docs"))), liquibase);
        Mockito.verify(liquibase).generateDocumentation("/tmp/docs");
    }

    @Test
    void testHelpPage() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MigrationTestSupport.createSubparser(generateDocsCommand)
                .printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8.name()))
                .isEqualToNormalizingNewlines("usage: db generate-docs [-h] [--migrations MIGRATIONS-FILE]\n"
                        + "          [--catalog CATALOG] [--schema SCHEMA] [file] output\n"
                        + "\n"
                        + "Generate documentation about the database state.\n"
                        + "\n"
                        + "positional arguments:\n"
                        + "  file                   application configuration file\n"
                        + "  output                 output directory\n"
                        + "\n"
                        + "named arguments:\n"
                        + "  -h, --help             show this help message and exit\n"
                        + "  --migrations MIGRATIONS-FILE\n"
                        + "                         the file containing  the  Liquibase migrations for\n"
                        + "                         the application\n"
                        + "  --catalog CATALOG      Specify  the   database   catalog   (use  database\n"
                        + "                         default if omitted)\n"
                        + "  --schema SCHEMA        Specify the database schema  (use database default\n"
                        + "                         if omitted)\n");
    }
}
