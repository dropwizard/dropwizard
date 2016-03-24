package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.JarLocation;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CliTest {
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    private final JarLocation location = mock(JarLocation.class);
    private final Application<Configuration> app = new Application<Configuration>() {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    };
    private final Bootstrap<Configuration> bootstrap = new Bootstrap<>(app);
    private final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
    private final CheckCommand<Configuration> command = spy(new CheckCommand<>(app));
    private Cli cli;

    @BeforeClass
    public static void init() {
        // Set default locale to English because some tests assert localized error messages
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterClass
    public static void shutdown() {
        Locale.setDefault(DEFAULT_LOCALE);
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        when(location.toString()).thenReturn("dw-thing.jar");
        when(location.getVersion()).thenReturn(Optional.of("1.0.0"));
        bootstrap.addCommand(command);

        doNothing().when(command).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));

        this.cli = new Cli(location, bootstrap, stdOut, stdErr);
    }

    @Test
    public void handlesShortVersionCommands() throws Exception {
        assertThat(cli.run("-v"))
                .isTrue();

        assertThat(stdOut.toString())
                .isEqualTo(String.format("1.0.0%n"));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    public void handlesLongVersionCommands() throws Exception {
        assertThat(cli.run("--version"))
                .isTrue();

        assertThat(stdOut.toString())
                .isEqualTo(String.format("1.0.0%n"));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    public void handlesMissingVersions() throws Exception {
        when(location.getVersion()).thenReturn(Optional.empty());
        final Cli newCli = new Cli(location, bootstrap, stdOut, stdErr);

        assertThat(newCli.run("--version"))
                .isTrue();

        assertThat(stdOut.toString())
                .isEqualTo(String.format("No application version detected. Add a Implementation-Version entry to your JAR's manifest to enable this.%n"));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    public void handlesZeroArgumentsAsHelpCommand() throws Exception {
        assertThat(cli.run())
                .isTrue();

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
                        "usage: java -jar dw-thing.jar [-h] [-v] {check} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check}                available commands%n" +
                                "%n" +
                                "optional arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    public void handlesShortHelpCommands() throws Exception {
        assertThat(cli.run("-h"))
                .isTrue();

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
                        "usage: java -jar dw-thing.jar [-h] [-v] {check} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check}                available commands%n" +
                                "%n" +
                                "optional arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    public void handlesLongHelpCommands() throws Exception {
        assertThat(cli.run("--help"))
                .isTrue();

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
                        "usage: java -jar dw-thing.jar [-h] [-v] {check} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check}                available commands%n" +
                                "%n" +
                                "optional arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void handlesShortHelpSubcommands() throws Exception {
        assertThat(cli.run("check", "-h"))
                .isTrue();

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
                        "usage: java -jar dw-thing.jar check [-h] [file]%n" +
                                "%n" +
                                "Parses and validates the configuration file%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  file                   application configuration file%n" +
                                "%n" +
                                "optional arguments:%n" +
                                "  -h, --help             show this help message and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();

        verify(command, never()).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void handlesLongHelpSubcommands() throws Exception {
        assertThat(cli.run("check", "--help"))
                .isTrue();

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
                        "usage: java -jar dw-thing.jar check [-h] [file]%n" +
                                "%n" +
                                "Parses and validates the configuration file%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  file                   application configuration file%n" +
                                "%n" +
                                "optional arguments:%n" +
                                "  -h, --help             show this help message and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();

        verify(command, never()).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));
    }

    @Test
    public void rejectsBadCommandFlags() throws Exception {
        assertThat(cli.run("--yes"))
                .isFalse();

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr.toString())
                .isEqualTo(String.format(
                        "unrecognized arguments: '--yes'%n" +
                                "usage: java -jar dw-thing.jar [-h] [-v] {check} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check}                available commands%n" +
                                "%n" +
                                "optional arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));
    }

    @Test
    public void rejectsBadSubcommandFlags() throws Exception {
        assertThat(cli.run("check", "--yes"))
                .isFalse();

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr.toString())
                .isEqualTo(String.format(
                        "unrecognized arguments: '--yes'%n" +
                                "usage: java -jar dw-thing.jar check [-h] [file]%n" +
                                "%n" +
                                "Parses and validates the configuration file%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  file                   application configuration file%n" +
                                "%n" +
                                "optional arguments:%n" +
                                "  -h, --help             show this help message and exit%n"
                ));
    }

    @Test
    public void rejectsBadSubcommands() throws Exception {
        assertThat(cli.run("plop"))
                .isFalse();

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr.toString())
                .isEqualTo(String.format(
                        "invalid choice: 'plop' (choose from 'check')%n" +
                                "usage: java -jar dw-thing.jar [-h] [-v] {check} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check}                available commands%n" +
                                "%n" +
                                "optional arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));
    }

    @Test
    public void runsCommands() throws Exception {
        assertThat(cli.run("check"))
                .isTrue();

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr.toString())
                .isEmpty();

        verify(command).run(eq(bootstrap), any(Namespace.class), any(Configuration.class));
    }
}
