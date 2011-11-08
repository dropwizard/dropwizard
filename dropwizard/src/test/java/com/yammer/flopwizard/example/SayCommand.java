package com.yammer.flopwizard.example;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Configuration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@SuppressWarnings("NullableProblems")
public class SayCommand extends ConfiguredCommand<ExampleConfiguration> {
    public SayCommand() {
        super(ExampleConfiguration.class, "say", "Prints out the saying to console");
    }

    @Override
    public Options getOptions() {
        return new Options().addOption("v", "verbose", false, "yell it a lot");
    }

    @Override
    protected void run(Service service,
                       ExampleConfiguration configuration,
                       CommandLine params) throws Exception {
        final String saying = configuration.getSaying();
        int max = 1;
        if (params.hasOption("verbose")) {
            max = 10;
        }

        for (int i = 0; i < max; i++) {
            System.err.println(saying);
        }
    }
}
