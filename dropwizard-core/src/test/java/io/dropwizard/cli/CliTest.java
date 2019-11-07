package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.JarLocation;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @BeforeAll
    public static void init() {
        // Set default locale to English because some tests assert localized error messages
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterAll
    public static void shutdown() {
        Locale.setDefault(DEFAULT_LOCALE);
    }

    @BeforeEach
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
        cli.run("-v");

        assertThat(stdOut.toString())
                .isEqualTo(String.format("1.0.0%n"));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    public void handlesLongVersionCommands() throws Exception {
        cli.run("--version");

        assertThat(stdOut.toString())
                .isEqualTo(String.format("1.0.0%n"));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    public void handlesMissingVersions() throws Exception {
        when(location.getVersion()).thenReturn(Optional.empty());
        final Cli newCli = new Cli(location, bootstrap, stdOut, stdErr);

        newCli.run("--version");

        assertThat(stdOut.toString())
                .isEqualTo(String.format("No application version detected. Add a Implementation-Version entry to your JAR's manifest to enable this.%n"));

        assertThat(stdErr.toString())
                .isEmpty();
    }

    @Test
    public void handlesZeroArgumentsAsHelpCommand() throws Exception {
        cli.run();

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
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
    public void handlesShortHelpCommands() throws Exception {
        cli.run("-h");

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
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
    public void handlesLongHelpCommands() throws Exception {
        cli.run("--help");

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
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
    @SuppressWarnings("unchecked")
    public void handlesShortHelpSubcommands() throws Exception {
        cli.run("check", "-h");

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
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

        verify(command, never()).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void handlesLongHelpSubcommands() throws Exception {
        cli.run("check", "--help");

        assertThat(stdOut.toString())
                .isEqualTo(String.format(
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

        verify(command, never()).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));
    }

    @Test
    public void rejectsBadCommandFlags() throws Exception {
        assertThatThrownBy(() -> { cli.run("--yes"); })
            .isInstanceOf(Exception.class);

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
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));
    }

    @Test
    public void rejectsBadSubcommandFlags() throws Exception {
        assertThatThrownBy(() -> { cli.run("check", "--yes"); })
            .isInstanceOf(Exception.class);

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
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n"
                ));
    }

    @Test
    public void rejectsBadSubcommands() throws Exception {
        assertThatThrownBy(() -> { cli.run("plop"); })
            .isInstanceOf(Exception.class);

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
                                "named arguments:%n" +
                                "  -h, --help             show this help message and exit%n" +
                                "  -v, --version          show the application version and exit%n"
                ));
    }

    @Test
    public void runsCommands() throws Exception {
        cli.run("check");

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

        assertThatThrownBy(() -> { cli.run("check"); })
            .isInstanceOf(Exception.class);

        assertThat(stdOut.toString())
                .isEmpty();

        assertThat(stdErr.toString())
                .isEqualTo(String.format("I'm a bad exception%n"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void unhandledExceptionsCustomCommand() throws Exception {
        doThrow(new BadAppException()).when(command).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));

        assertThatThrownBy(() -> { cli.run("custom"); })
            .isInstanceOf(Exception.class);

        assertThat(stdOut.toString())
            .isEqualTo(String.format("I did not expect this!%n"));

        assertThat(stdErr.toString())
            .isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void unhandledExceptionsCustomCommandDebug() throws Exception {
        doThrow(new BadAppException()).when(command).run(any(Bootstrap.class), any(Namespace.class), any(Configuration.class));

        assertThatThrownBy(() -> { cli.run("custom", "--debug"); })
            .isInstanceOf(Exception.class);

        assertThat(stdOut.toString())
            .isEmpty();

        assertThat(stdErr.toString())
            .startsWith(String.format("java.lang.RuntimeException: I did not expect this!%n" +
                "\tat io.dropwizard.cli.CliTest$CustomCommand.run(CliTest.java"));
    }
}
