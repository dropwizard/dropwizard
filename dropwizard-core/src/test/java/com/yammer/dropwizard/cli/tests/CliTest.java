package com.yammer.dropwizard.cli.tests;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.cli.Cli;
import com.yammer.dropwizard.cli.Command;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CliTest {
    private static final String[] EMPTY = new String[0];

    @Parameters(commandNames = { "other" }, commandDescription = "Another command")
    public static class OtherCommand implements Command {
        @Parameter(names = { "-n", "--names" }, description = "<name>", arity = 1)
        private String name;

        private boolean ran;

        @Override
        @SuppressWarnings("NewExceptionWithoutArguments")
        public void run(AbstractService<?> service) throws Exception {
            this.ran = true;
            if ("monkey".equals(name)) {
                throw new RuntimeException("bad thing");
            } else if ("dog".equals(name)) {
                throw new RuntimeException();
            }
        }

        public String getName() {
            return name;
        }

        public boolean hasRun() {
            return ran;
        }
    }

    private final AbstractService<?> service = mock(AbstractService.class);
    private final OtherCommand other = new OtherCommand();
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Cli cli = new Cli(service,
                                    ImmutableList.<Command>of(other),
                                    new PrintStream(output));

    @Test
    public void displaysUsageWhenGivenNoCommand() throws Exception {
        final Optional<String> result = cli.run(EMPTY);

        assertThat(result.isPresent())
                .isTrue();

        assertThat(output.toString())
                .isEqualTo(
                        "Usage: java -jar mockito-all-1.9.5-rc1.jar [options] [command] [command options]  Commands:\n" +
                                "    help      Show usage information\n" +
                                "      Usage: help [options] [command1 ... command2]\n" +
                                "    other      Another command\n" +
                                "      Usage: other [options]      \n" +
                                "        Options:\n" +
                                "          -n, --names   <name>\n\n");
    }

    @Test
    public void displaysUsageWhenGivenHelp() throws Exception {
        final Optional<String> result = cli.run(new String[]{ "help" });

        assertThat(result.isPresent())
                .isFalse();

        assertThat(output.toString())
                .isEqualTo("Usage: java -jar mockito-all-1.9.5-rc1.jar [options] [command] [command options]  Commands:\n" +
                                "    help      Show usage information\n" +
                                "      Usage: help [options] [command1 ... command2]\n" +
                                "    other      Another command\n" +
                                "      Usage: other [options]      \n" +
                                "        Options:\n" +
                                "          -n, --names   <name>\n\n");
    }

    @Test
    public void displaysCommandUsageWhenGivenSpecificHelp() throws Exception {
        final Optional<String> result = cli.run(new String[]{ "help", "other" });

        assertThat(result.isPresent())
                .isFalse();

        assertThat(output.toString())
                .isEqualTo("Another command\n" +
                                   "Usage: other [options]\n" +
                                   "  Options:\n" +
                                   "    -n, --names   <name>\n");
    }

    @Test
    public void runsTheGivenCommand() throws Exception {
        final Optional<String> result = cli.run(new String[]{ "other", "-n", "poop" });

        assertThat(result.isPresent())
                .isFalse();

        assertThat(other.hasRun())
                .isTrue();

        assertThat(other.getName())
                .isEqualTo("poop");
    }

    @Test
    public void handlesExceptionsThrownByCommands() throws Exception {
        final Optional<String> result = cli.run(new String[]{ "other", "-n", "monkey" });

        assertThat(result.isPresent())
                .isTrue();

        assertThat(result.get())
                .isEqualTo("bad thing");
    }

    @Test
    public void handlesMessagelessExceptionsThrownByCommands() throws Exception {
        final Optional<String> result = cli.run(new String[]{ "other", "-n", "dog" });

        assertThat(result.isPresent())
                .isTrue();

        assertThat(result.get())
                .isEqualTo("A java.lang.RuntimeException was thrown.");
    }
}
