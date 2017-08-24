package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.JarLocation;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

    private static final class BadAppException extends Exception {
        private static final long serialVersionUID = 1L;

        public static final String BAD_APP_EXCEPTION_STACK_TRACE = "BadAppException stack trace";

        public BadAppException() {
        }

        @Override
        public void printStackTrace(PrintWriter writer) {
            writer.println(BAD_APP_EXCEPTION_STACK_TRACE);
        }

        @Override
        public String getMessage() {
            return "I'm a bad exception";
        }
    }

    public static final class CustomCommand extends Command {
        protected CustomCommand() {
            super("custom", "I'm custom");
        }

        @Override
        public void configure(Subparser subparser) {
            subparser.addArgument("--debug")
                .action(Arguments.storeTrue());
        }

        @Override
        public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
            throw new RuntimeException("I did not expect this!");
        }

        @Override
        public void onError(Cli cli, Namespace namespace, Throwable e) {
            if (namespace.getBoolean("debug")) {
                super.onError(cli, namespace, e);
            } else {
                cli.getStdOut().println(e.getMessage());
            }
        }
    }

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
        bootstrap.addCommand(new CustomCommand());

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
                        "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
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
                        "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
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
                        "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
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
                                "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
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
                        "invalid choice: 'plop' (choose from 'check', 'custom')%n" +
                                "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
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

    @Test
    @SuppressWarnings("unchecked")
    public void unhandledExceptionsMessagesArePrintedForCheck() throws Exception {
        doThrow(new BadAppException()).when(command).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));

        assertThat(cli.run("check"))
                .isFalse();

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr.toString())
                .isEqualTo(String.format("I'm a bad exception%n"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void unhandledExceptionsCustomCommand() throws Exception {
        doThrow(new BadAppException()).when(command).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));

        assertThat(cli.run("custom"))
            .isFalse();

        assertThat(stdOut.toString())
            .isEqualTo(String.format("I did not expect this!%n"));

        assertThat(stdErr.toString())
            .isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void unhandledExceptionsCustomCommandDebug() throws Exception {
        doThrow(new BadAppException()).when(command).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));

        assertThat(cli.run("custom", "--debug"))
            .isFalse();

        assertThat(stdOut.toString())
            .isEmpty();

        assertThat(stdErr.toString())
            .startsWith(String.format("java.lang.RuntimeException: I did not expect this!%n" +
                "\tat io.dropwizard.cli.CliTest$CustomCommand.run(CliTest.java"));
    }
}
