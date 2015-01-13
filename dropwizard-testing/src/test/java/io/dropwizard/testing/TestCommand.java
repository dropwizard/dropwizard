package io.dropwizard.testing;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class TestCommand extends Command {
    public String output = null;

    protected TestCommand() {
        super("Test", "A simple command for use with testing.");
    }

    @Override
    public void configure(Subparser subparser) {

    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        output = "success";
    }
}
