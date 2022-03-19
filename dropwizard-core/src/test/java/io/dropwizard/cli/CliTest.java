package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.JarLocation;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.internal.UnrecognizedArgumentException;
import net.sourceforge.argparse4j.internal.UnrecognizedCommandException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Objects;
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

class CliTest {
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    private final JarLocation location = mock(JarLocation.class);
    private final Application<Configuration> app = new Application<Configuration>() {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    };

    private static final class BadAppException extends Exception {
        private static final long serialVersionUID = 1L;

        static final String BAD_APP_EXCEPTION_STACK_TRACE = "BadAppException stack trace";

        BadAppException() {
            super("I'm a bad exception");
        }

        @Override
        public void printStackTrace(PrintWriter writer) {
            writer.println(BAD_APP_EXCEPTION_STACK_TRACE);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BadAppException that = (BadAppException) o;

            return Objects.equals(this.getMessage(), that.getMessage());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getMessage());
        }
    }

    public static final class CustomCommand extends Command {
        CustomCommand() {
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

    @BeforeAll
    static void init() {
        // Set default locale to English because some tests assert localized error messages
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterAll
    static void shutdown() {
        Locale.setDefault(DEFAULT_LOCALE);
    }

    @BeforeEach
    void setUp() throws Exception {
        when(location.toString()).thenReturn("dw-thing.jar");
        when(location.getVersion()).thenReturn(Optional.of("1.0.0"));
        bootstrap.addCommand(command);
        bootstrap.addCommand(new CustomCommand());

        doNothing().when(command).run(any(), any(Namespace.class), any(Configuration.class));

        this.cli = new Cli(location, bootstrap, stdOut, stdErr);
    }

    @Test
    void handlesShortVersionCommands() throws Exception {
        assertThat(cli.run("-v"))
                .isEmpty();

        assertThat(stdOut)
                .hasToString(String.format("1.0.0%n"));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    void handlesLongVersionCommands() throws Exception {
        assertThat(cli.run("--version"))
                .isEmpty();

        assertThat(stdOut)
                .hasToString(String.format("1.0.0%n"));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    void handlesMissingVersions() throws Exception {
        when(location.getVersion()).thenReturn(Optional.empty());
        final Cli newCli = new Cli(location, bootstrap, stdOut, stdErr);

        assertThat(newCli.run("--version"))
                .isEmpty();

        assertThat(stdOut)
                .hasToString(String.format("No application version detected. Add a Implementation-Version entry to your JAR's manifest to enable this.%n"));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    void handlesZeroArgumentsAsHelpCommand() throws Exception {
        assertThat(cli.run())
                .isEmpty();

        assertThat(stdOut)
                .hasToString(String.format(
                        "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
                                "%n" +
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    void handlesShortHelpCommands() throws Exception {
        assertThat(cli.run("-h"))
                .isEmpty();

        assertThat(stdOut)
                .hasToString(String.format(
                        "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
                                "%n" +
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    void handlesLongHelpCommands() throws Exception {
        assertThat(cli.run("--help"))
                .isEmpty();

        assertThat(stdOut)
                .hasToString(String.format(
                        "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
                                "%n" +
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    void handlesShortHelpSubcommands() throws Exception {
        assertThat(cli.run("check", "-h"))
                .isEmpty();

        assertThat(stdOut)
                .hasToString(String.format(
                        "usage: java -jar dw-thing.jar check [-h] [file]%n" +
                                "%n" +
                                "Parses and validates the configuration file%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  file                   application configuration file%n" +
                                "%n" +
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();

        verify(command, never()).run(any(), any(Namespace.class), any(Configuration.class));
    }

    @Test
    void handlesLongHelpSubcommands() throws Exception {
        assertThat(cli.run("check", "--help"))
                .isEmpty();

        assertThat(stdOut)
                .hasToString(String.format(
                        "usage: java -jar dw-thing.jar check [-h] [file]%n" +
                                "%n" +
                                "Parses and validates the configuration file%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  file                   application configuration file%n" +
                                "%n" +
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n"
                ));

        assertThat(stdErr.toString())
                .isEmpty();

        verify(command, never()).run(any(), any(Namespace.class), any(Configuration.class));
    }

    @Test
    void rejectsBadCommandFlags() throws Exception {
        assertThat(cli.run("--yes"))
                .hasValueSatisfying(t -> assertThat(t).isInstanceOf(UnrecognizedArgumentException.class));

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr)
                .hasToString(String.format(
                        "unrecognized arguments: '--yes'%n" +
                                "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
                                "%n" +
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));
    }

    @Test
    void rejectsBadSubcommandFlags() throws Exception {
        assertThat(cli.run("check", "--yes"))
                .hasValueSatisfying(t -> assertThat(t).isExactlyInstanceOf(UnrecognizedArgumentException.class));

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr)
                .hasToString(String.format(
                        "unrecognized arguments: '--yes'%n" +
                                "usage: java -jar dw-thing.jar check [-h] [file]%n" +
                                "%n" +
                                "Parses and validates the configuration file%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  file                   application configuration file%n" +
                                "%n" +
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n"
                ));
    }

    @Test
    void rejectsBadSubcommands() throws Exception {
        assertThat(cli.run("plop"))
                .hasValueSatisfying(t -> assertThat(t).isInstanceOf(UnrecognizedCommandException.class));

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr)
                .hasToString(String.format(
                        "invalid choice: 'plop' (choose from 'check', 'custom')%n" +
                                "usage: java -jar dw-thing.jar [-h] [-v] {check,custom} ...%n" +
                                "%n" +
                                "positional arguments:%n" +
                                "  {check,custom}         available commands%n" +
                                "%n" +
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));
    }

    @Test
    void runsCommands() throws Exception {
        assertThat(cli.run("check"))
                .isEmpty();

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr.toString())
                .isEmpty();

        verify(command).run(eq(bootstrap), any(Namespace.class), any(Configuration.class));
    }

    @Test
    void unhandledExceptionsMessagesArePrintedForCheck() throws Exception {
        doThrow(new BadAppException()).when(command).run(any(), any(Namespace.class), any(Configuration.class));

        assertThat(cli.run("check"))
                .hasValue(new BadAppException());

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr)
                .hasToString(String.format("I'm a bad exception%n"));
    }

    @Test
    void unhandledExceptionsCustomCommand() throws Exception {
        doThrow(new BadAppException()).when(command).run(any(), any(Namespace.class), any(Configuration.class));

        assertThat(cli.run("custom"))
                .hasValueSatisfying(t -> assertThat(t).isInstanceOf(RuntimeException.class).hasMessage("I did not expect this!"));

        assertThat(stdOut)
            .hasToString(String.format("I did not expect this!%n"));

        assertThat(stdErr.toString())
            .isEmpty();
    }

    @Test
    void unhandledExceptionsCustomCommandDebug() throws Exception {
        doThrow(new BadAppException()).when(command).run(any(), any(Namespace.class), any(Configuration.class));

        assertThat(cli.run("custom", "--debug"))
                .hasValueSatisfying(t -> assertThat(t).isInstanceOf(RuntimeException.class).hasMessage("I did not expect this!"));

        assertThat(stdOut.toString())
            .isEmpty();

        assertThat(stdErr.toString())
            .startsWith(String.format("java.lang.RuntimeException: I did not expect this!%n" +
                "\tat io.dropwizard.cli.CliTest$CustomCommand.run(CliTest.java"));
    }
}
