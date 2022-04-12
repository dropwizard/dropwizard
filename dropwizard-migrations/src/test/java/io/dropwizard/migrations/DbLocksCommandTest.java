package io.dropwizard.migrations;

import io.dropwizard.util.Maps;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
class DbLocksCommandTest {

    private final DbLocksCommand<TestMigrationConfiguration> locksCommand = new DbLocksCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");

    @Test
    void testRelease() throws Exception {
        // We can't create locks in the database, so use mocks
        final Liquibase liquibase = Mockito.mock(Liquibase.class);
        locksCommand.run(new Namespace(Maps.of("list", false, "release", true)), liquibase);
        Mockito.verify(liquibase).forceReleaseLocks();
    }

    @Test
    void testListLocks() throws Exception {
        final PrintStream printStream = new PrintStream(new ByteArrayOutputStream());
        locksCommand.setPrintStream(printStream);

        // We can't create locks in the database, so use mocks
        final Liquibase liquibase = Mockito.mock(Liquibase.class);
        locksCommand.run(new Namespace(Maps.of("list", true, "release", false)), liquibase);
        Mockito.verify(liquibase).reportLocks(printStream);
    }

    @Test
    void testFailsWhenNoListOrRelease() {
        final Liquibase liquibase = Mockito.mock(Liquibase.class);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> locksCommand.run(new Namespace(Maps.of("list", false, "release", false)),
                liquibase))
            .withMessage("Must specify either --list or --force-release");
    }

    @Test
    void testFailsWhenBothListAndRelease() {
        final Liquibase liquibase = Mockito.mock(Liquibase.class);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> locksCommand.run(new Namespace(Maps.of("list", true, "release", true)),
                liquibase))
            .withMessage("Must specify either --list or --force-release");
    }

    @Test
    void testPrintHelp() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        MigrationTestSupport.createSubparser(locksCommand).printHelp(new PrintWriter(new OutputStreamWriter(out, UTF_8), true));
        assertThat(out.toString(UTF_8.name())).isEqualToNormalizingNewlines(
            "usage: db locks [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]\n" +
                "          [--schema SCHEMA] [-l] [-r] [file]\n" +
                "\n" +
                "Manage database migration locks\n" +
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
                "  -l, --list             list all open locks\n" +
                "  -r, --force-release    forcibly release all open locks\n");
    }
}
