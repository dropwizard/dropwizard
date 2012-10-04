package com.example.helloworld.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.example.helloworld.HelloWorldConfiguration;
import com.example.helloworld.core.Template;
import com.google.common.base.Optional;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.logging.Log;

@Parameters(commandNames = "render",
            commandDescription = "Render the template data to console")
public class RenderCommand extends ConfiguredCommand<HelloWorldConfiguration> {
    private static final Log LOG = Log.forClass(RenderCommand.class);

    @SuppressWarnings({ "FieldCanBeLocal", "FieldMayBeFinal" })
    @Parameter(names = {"-i", "--include-default"},
               description = "Also render the template with the default name")
    private boolean includeDefault = false;

    @Override
    protected void run(Bootstrap<HelloWorldConfiguration> bootstrap,
                       HelloWorldConfiguration configuration) throws Exception {
        final Template template = configuration.buildTemplate();

        if (includeDefault) {
            LOG.info("DEFAULT => {}", template.render(Optional.<String>absent()));
        }

        for (String name : getArguments()) {
            for (int i = 0; i < 1000; i++) {
                LOG.info("{} => {}", name, template.render(Optional.of(name)));
                Thread.sleep(1000);
            }
        }
    }
}
