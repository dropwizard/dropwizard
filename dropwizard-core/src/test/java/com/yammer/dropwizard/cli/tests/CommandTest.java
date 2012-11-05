package com.yammer.dropwizard.cli.tests;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.Command;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.apache.commons.cli.*;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Ignore("DW lifecycle doesn't play well with Surefire")
public class CommandTest {
    public static class ExampleCommandWithOptions extends Command {
        private String first, last;
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


    public static class ExampleCommandWithOptionGroups extends Command {
        private String first, last;
        private boolean verbose = false;

        public ExampleCommandWithOptionGroups() {
            super("name", "description");
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            OptionGroup group = new OptionGroup();
            group.addOption(OptionBuilder.create("C"));
            group.addOption(OptionBuilder.create("P"));
            options.addOptionGroup(group);
            return options;
        }

        @Override
        protected void run(AbstractService<?> service,
                           CommandLine params) throws Exception {
            // Do nothing
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
    private final ExampleCommandWithOptionGroups commandWithOptionGroups = new ExampleCommandWithOptionGroups();

    @Test
    public void hasAName() throws Exception {
        assertThat(commandWithOptions.getName(),
            is("name"));
    }

    @Test
    public void hasADescription() throws Exception {
        assertThat(commandWithOptions.getDescription(),
            is("description"));
    }

    @Test
    public void hasEmptyOptionsByDefault() throws Exception {
        Command command = new Command("name", "description") {
            @Override
            protected void run(AbstractService<?> service,
                               CommandLine params) throws Exception {
            }
        };
        assertThat(command.getOptions().toString(),
            is(new Options().toString()));
    }

    @Test
    public void acceptsMultipleArguments() throws Exception {
        commandWithOptions.run(new DummyService(),
            new String[]{"-f", "first", "-l", "last", "-v"});

        assertTrue(commandWithOptions.isVerbose());
        assertThat(commandWithOptions.getFirst(), is("first"));
        assertThat(commandWithOptions.getLast(), is("last"));
    }


    @Test
    public void acceptsOptionGroups() throws Exception {
        try{
            commandWithOptionGroups.run(new DummyService(),
                    new String[]{"-P", "-C"});
              fail("should have thrown an AlreadySelectedException but didn't");
        }catch (AlreadySelectedException e){
             // Everything right !
        }

    }


    @Test(expected = UnrecognizedOptionException.class)
    public void failsWithInvalidOption() throws Exception {
        commandWithOptions.run(new DummyService(), new String[]{"-x"});
    }
}
