package com.yammer.dropwizard.cli.tests;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.cli.Command;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CommandTest {
    public static class ExampleCommand extends Command {
        public ExampleCommand() {
            super("name", "description");
        }

        @Override
        protected void run(AbstractService<?> service,
                           CommandLine params) throws Exception {
        }
    }

    private final ExampleCommand command = new ExampleCommand();

    @Test
    public void hasAName() throws Exception {
        assertThat(command.getName(),
                   is("name"));
    }

    @Test
    public void hasADescription() throws Exception {
        assertThat(command.getDescription(),
                   is("description"));
    }

    @Test
    public void hasEmptyOptionsByDefault() throws Exception {
        assertThat(command.getOptions().toString(),
                   is(new Options().toString()));
    }
}
