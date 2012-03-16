package com.example.helloworld.cli;

import com.example.helloworld.HelloWorldConfiguration;
import com.example.helloworld.core.Template;
import com.google.common.base.Optional;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.logging.Log;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class RenderCommand extends ConfiguredCommand<HelloWorldConfiguration> {
    private static final Log LOG = Log.forClass(RenderCommand.class);

    public RenderCommand() {
        super("render", "Renders the configured template to the console.");
    }

    @Override
    protected String getConfiguredSyntax() {
        return "[name1 name2]";
    }

    @Override
    public Options getOptions() {
        final Options options = new Options();
        options.addOption("i", "include-default", false,
                          "Also render the template with the default name");
        return options;
    }

    @Override
    protected void run(AbstractService<HelloWorldConfiguration> service,
                       HelloWorldConfiguration configuration,
                       CommandLine params) throws Exception {
        final Template template = configuration.buildTemplate();

        if (params.hasOption("include-default")) {
            LOG.info("DEFAULT => {}", template.render(Optional.<String>absent()));
        }

        for (String name : params.getArgs()) {
            for (int i = 0; i < 1000; i++) {
                LOG.info("{} => {}", name, template.render(Optional.of(name)));
                Thread.sleep(1000);
            }
        }
    }
}
