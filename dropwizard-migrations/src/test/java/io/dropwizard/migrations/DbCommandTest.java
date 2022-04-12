package io.dropwizard.migrations;

import net.sourceforge.argparse4j.inf.Namespace;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
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
class DbCommandTest {

    private final DbCommand<TestMigrationConfiguration> dbCommand = new DbCommand<>("db",
        new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, "migrations.xml");

    @Test
    void testRunSubCommand() throws Exception {
        final String databaseUrl = MigrationTestSupport.getDatabaseUrl();
        final TestMigrationConfiguration conf = MigrationTestSupport.createConfiguration(databaseUrl);
        dbCommand.run(null, new Namespace(Collections.singletonMap("subcommand", "migrate")), conf);

        try (Handle handle = Jdbi.create(databaseUrl, "sa", "").open()) {
            assertThat(handle.createQuery("select count(*) from persons")
                .mapTo(Integer.class)
                .first()).isEqualTo(1);
        }
    }

    @Test
    void testPrintHelp() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MigrationTestSupport.createSubparser(dbCommand).printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8.name())).isEqualToNormalizingNewlines(
            "usage: db db [-h]\n" +
                "          {calculate-checksum,clear-checksums,drop-all,dump,fast-forward,generate-docs,locks,migrate,prepare-rollback,rollback,status,tag,test}\n" +
                "          ...\n" +
                "\n" +
                "Run database migration tasks\n" +
                "\n" +
                "positional arguments:\n" +
                "  {calculate-checksum,clear-checksums,drop-all,dump,fast-forward,generate-docs,locks,migrate,prepare-rollback,rollback,status,tag,test}\n" +
                "\n" +
                "named arguments:\n" +
                "  -h, --help             show this help message and exit\n");
    }
}
