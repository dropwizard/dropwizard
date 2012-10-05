package com.example.helloworld.cli;

import com.example.helloworld.HelloWorldConfiguration;
import com.example.helloworld.core.Template;
import com.google.common.base.Optional;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.logging.Log;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class RenderCommand extends ConfiguredCommand<HelloWorldConfiguration> {
    private static final Log LOG = Log.forClass(RenderCommand.class);

    public RenderCommand() {
        super("render", "Render the template data to console");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
        subparser.addArgument("-i", "--include-default")
                 .action(Arguments.storeTrue())
                 .dest("include-default")
                 .help("Also render the template with the default name");
        subparser.addArgument("names").nargs("*");
    }

    @Override
    protected void run(Bootstrap<HelloWorldConfiguration> bootstrap,
                       Namespace namespace,
                       HelloWorldConfiguration configuration) throws Exception {
        final Template template = configuration.buildTemplate();

        if (namespace.getBoolean("include-default")) {
            LOG.info("DEFAULT => {}", template.render(Optional.<String>absent()));
        }

        for (String name : namespace.<String>getList("names")) {
            for (int i = 0; i < 1000; i++) {
                LOG.info("{} => {}", name, template.render(Optional.of(name)));
                Thread.sleep(1000);
            }
        }
    }
}
