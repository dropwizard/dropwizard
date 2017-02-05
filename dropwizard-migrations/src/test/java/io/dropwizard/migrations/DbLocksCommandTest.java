package io.dropwizard.migrations;

import com.google.common.collect.ImmutableMap;
import liquibase.Liquibase;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@NotThreadSafe
public class DbLocksCommandTest extends AbstractMigrationTest {

    private DbLocksCommand<TestMigrationConfiguration> locksCommand = new DbLocksCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");

    @Test
    public void testRelease() throws Exception {
        // We can't create locks in the database, so use mocks
        final Liquibase liquibase = Mockito.mock(Liquibase.class);
        locksCommand.run(new Namespace(ImmutableMap.of("list", false, "release", true)), liquibase);
        Mockito.verify(liquibase).forceReleaseLocks();
    }

    @Test
    public void testListLocks() throws Exception {
        final PrintStream printStream = new PrintStream(new ByteArrayOutputStream());
        locksCommand.setPrintStream(printStream);

        // We can't create locks in the database, so use mocks
        final Liquibase liquibase = Mockito.mock(Liquibase.class);
        locksCommand.run(new Namespace(ImmutableMap.of("list", true, "release", false)), liquibase);
        Mockito.verify(liquibase).reportLocks(printStream);
    }

    @Test
    public void testFailsWhenNoListOrRelease() throws Exception {
        final Liquibase liquibase = Mockito.mock(Liquibase.class);
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> locksCommand.run(new Namespace(ImmutableMap.of("list", false, "release", false)),
                liquibase))
            .withMessage("Must specify either --list or --force-release");
    }

    @Test
    public void testFailsWhenBothListAndRelease() throws Exception {
        final Liquibase liquibase = Mockito.mock(Liquibase.class);
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> locksCommand.run(new Namespace(ImmutableMap.of("list", true, "release", true)),
                liquibase))
            .withMessage("Must specify either --list or --force-release");
    }

    @Test
    public void testPrintHelp() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        createSubparser(locksCommand).printHelp(new PrintWriter(new OutputStreamWriter(out, UTF_8), true));
        assertThat(out.toString(UTF_8)).isEqualTo(String.format(
            "usage: db locks [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]%n" +
                "          [--schema SCHEMA] [-l] [-r] [file]%n" +
                "%n" +
                "Manage database migration locks%n" +
                "%n" +
                "positional arguments:%n" +
                "  file                   application configuration file%n" +
                "%n" +
                "optional arguments:%n" +
                "  -h, --help             show this help message and exit%n" +
                "  --migrations MIGRATIONS-FILE%n" +
                "                         the file containing  the  Liquibase migrations for%n" +
                "                         the application%n" +
                "  --catalog CATALOG      Specify  the   database   catalog   (use  database%n" +
                "                         default if omitted)%n" +
                "  --schema SCHEMA        Specify the database schema  (use database default%n" +
                "                         if omitted)%n" +
                "  -l, --list             list all open locks%n" +
                "  -r, --force-release    forcibly release all open locks%n"));
    }
}
