package com.example.helloworldcli;

import com.example.helloworldcli.cli.GreetCommand;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldCliApplication extends Application<HelloWorldCliConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldCliApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<HelloWorldCliConfiguration> bootstrap) {
        bootstrap.addCommand(new GreetCommand());
    }

    @Override
    public void run(HelloWorldCliConfiguration configuration, Environment environment) throws Exception {

    }

}
