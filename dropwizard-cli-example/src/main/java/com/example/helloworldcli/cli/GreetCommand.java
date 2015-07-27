package com.example.helloworldcli.cli;

import com.example.helloworldcli.HelloWorldCliConfiguration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class GreetCommand extends ConfiguredCommand<HelloWorldCliConfiguration> {

    public GreetCommand() {
        super("message", "Greet the named user");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
        subparser.addArgument("user")
                .action(Arguments.store())
                .dest("user")
                .help("The name of the user to greet");
    }

    @Override
    protected void run(Bootstrap<HelloWorldCliConfiguration> bootstrap,
                       Namespace namespace,
                       HelloWorldCliConfiguration configuration) throws Exception {

        String user = namespace.get("user");
        String greeting = configuration.getGreeting();
        System.out.printf(greeting, user);
        System.out.println();

    }
}
