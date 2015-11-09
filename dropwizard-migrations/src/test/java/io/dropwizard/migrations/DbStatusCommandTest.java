package io.dropwizard.migrations;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbStatusCommandTest extends AbstractMigrationTest {

    private final DbStatusCommand<TestMigrationConfiguration> statusCommand =
            new DbStatusCommand<>(new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class);
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private TestMigrationConfiguration conf;

    @Before
    public void setUp() throws Exception {
        final String databaseUrl = "jdbc:h2:mem:" + UUID.randomUUID();
        conf = createConfiguration(databaseUrl);

        statusCommand.setOutputStream(new PrintStream(baos));
    }

    @Test
    public void testRunOnMigratedDb() throws Exception {
        final String existedDbPath = new File(Resources.getResource("test-db.mv.db").toURI()).getAbsolutePath();
        final String existedDbUrl = "jdbc:h2:" + StringUtils.removeEnd(existedDbPath, ".mv.db");
        final TestMigrationConfiguration existedDbConf = createConfiguration(existedDbUrl);

        statusCommand.run(null, new Namespace(ImmutableMap.<String, Object>of()), existedDbConf);
        assertThat(baos.toString("UTF-8")).matches("\\S+ is up to date" + System.lineSeparator());
    }

    @Test
    public void testRun() throws Exception {
        statusCommand.run(null, new Namespace(ImmutableMap.<String, Object>of()), conf);
        assertThat(baos.toString("UTF-8")).matches(
                "3 change sets have not been applied to \\S+" + System.lineSeparator());
    }

    @Test
    public void testVerbose() throws Exception {
        statusCommand.run(null, new Namespace(ImmutableMap.of("verbose", (Object) true)), conf);
        assertThat(baos.toString("UTF-8")).matches(
                "3 change sets have not been applied to \\S+" + System.lineSeparator() +
                        "\\s*migrations\\.xml::1::db_dev"  + System.lineSeparator() +
                        "\\s*migrations\\.xml::2::db_dev"  + System.lineSeparator() +
                        "\\s*migrations\\.xml::3::db_dev" + System.lineSeparator());
    }

    @Test
    public void testPrintHelp() throws Exception {
        createSubparser(statusCommand).printHelp(new PrintWriter(baos, true));
        assertThat(baos.toString("UTF-8")).isEqualTo(String.format(
                "usage: db status [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]%n" +
                        "          [--schema SCHEMA] [-v] [-i CONTEXTS] [file]%n" +
                        "%n" +
                        "Check for pending change sets.%n" +
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
                        "  -v, --verbose          Output verbose information%n" +
                        "  -i CONTEXTS, --include CONTEXTS%n" +
                        "                         include change sets from the given context%n"));
    }
}
