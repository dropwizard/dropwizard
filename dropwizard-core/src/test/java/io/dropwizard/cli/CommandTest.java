package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.JarLocation;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandTest {
    private static class TestCommand extends Command {
        protected TestCommand() {
            super("test", "test");
        }

        @Override
        public void configure(Subparser subparser) {
            subparser.addArgument("types").choices("a", "b", "c").help("Type to use");
        }

        @Override
        public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        }
    }

    private final Application<Configuration> app = new Application<Configuration>() {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    };

    private final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
    private final Command command = new TestCommand();
    private Cli cli;

    @Before
    public void setUp() throws Exception {
        final JarLocation location = mock(JarLocation.class);
        final Bootstrap<Configuration> bootstrap = new Bootstrap<>(app);
        when(location.toString()).thenReturn("dw-thing.jar");
        when(location.getVersion()).thenReturn(Optional.of("1.0.0"));
        bootstrap.addCommand(command);

        cli = new Cli(location, bootstrap, stdOut, stdErr);
    }

    @Test
    public void listHelpOnceOnArgumentOmission() throws Exception {
        assertThat(cli.run("test", "-h"))
            .isTrue();

        assertThat(stdOut.toString())
            .isEqualTo(String.format(
                "usage: java -jar dw-thing.jar test [-h] {a,b,c}%n" +
                    "%n" +
                    "test%n" +
                    "%n" +
                    "positional arguments:%n" +
                    "  {a,b,c}                Type to use%n" +
                    "%n" +
                    "optional arguments:%n" +
                    "  -h, --help             show this help message and exit%n"
            ));

        assertThat(stdErr.toString())
            .isEmpty();
    }
}
