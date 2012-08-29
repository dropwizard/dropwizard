package com.yammer.dropwizard.cli.tests;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.Command;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.*;

public class CommandTest {
    public static class ExampleCommandWithOptions extends Command {
        private String first;
        private String last;
        private boolean verbose = false;

        public ExampleCommandWithOptions() {
            super("name", "description");
        }

        @Override
        public Options getOptions() {
            return super.getOptions()
                .addOption("f", "first", true, "First option with arg")
                .addOption("l", "last", true, "Last option with arg")
                .addOption("v", "verbose", false, "Verbose or not");
        }

        @Override
        protected void run(AbstractService<?> service,
                           CommandLine params) throws Exception {
            this.first = params.getOptionValue("first");
            this.last = params.getOptionValue("last");
            this.verbose = params.hasOption("verbose");
        }

        public String getFirst() {
            return first;
        }

        public String getLast() {
            return last;
        }

        public boolean isVerbose() {
            return verbose;
        }
    }

    public static class DummyConfig extends Configuration {
    }

    public static class DummyService extends Service<DummyConfig> {
        public DummyService() {
            super("dummy");
        }

        @Override
        protected void initialize(DummyConfig configuration,
                                  Environment environment) throws Exception {
        }
    }

    private final ExampleCommandWithOptions commandWithOptions = new ExampleCommandWithOptions();

    @Test
    public void hasAName() throws Exception {
        assertThat(commandWithOptions.getName())
            .isEqualTo("name");
    }

    @Test
    public void hasADescription() throws Exception {
        assertThat(commandWithOptions.getDescription())
            .isEqualTo("description");
    }

    @Test
    public void hasEmptyOptionsByDefault() throws Exception {
        final Command command = new Command("name", "description") {
            @Override
            protected void run(AbstractService<?> service,
                               CommandLine params) throws Exception {
            }
        };
        assertThat(command.getOptions().toString())
            .isEqualTo(new Options().toString());
    }

    @Test
    public void acceptsMultipleArguments() throws Exception {
        commandWithOptions.run(new DummyService(),
            new String[]{"-f", "first", "-l", "last", "-v"});

        assertThat(commandWithOptions.isVerbose())
                .isTrue();
        assertThat(commandWithOptions.getFirst())
                .isEqualTo("first");
        assertThat(commandWithOptions.getLast())
                .isEqualTo("last");
    }

    @SuppressWarnings("ExpectedExceptionNeverThrown")
    @Test(expected = UnrecognizedOptionException.class)
    public void failsWithInvalidOption() throws Exception {
        commandWithOptions.run(new DummyService(), new String[]{"-x"});
    }
}
