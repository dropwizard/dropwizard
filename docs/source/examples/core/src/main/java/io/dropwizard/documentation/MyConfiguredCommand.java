package io.dropwizard.documentation;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

// core: MyConfiguredCommand
public class MyConfiguredCommand extends ConfiguredCommand<Configuration> {
    public MyConfiguredCommand() {
        // The name of our command is "hello" and the description printed is
        // "Prints a greeting"
        super("hello", "Prints a greeting");
    }

    @Override
    public void configure(Subparser subparser) {
        // Add a command line option
        subparser
                .addArgument("-u", "--user")
                .dest("user")
                .type(String.class)
                .required(true)
                .help("The user of the program");
    }

    @Override
    protected void run(Bootstrap<Configuration> bootstrap, Namespace namespace, Configuration configuration)
            throws Exception {
        System.out.println("Hello " + namespace.getString("user"));
    }
}
// core: MyConfiguredCommand
