package com.example.helloworld;

import com.example.helloworld.cli.RenderCommand;
import com.example.helloworld.core.Template;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.resources.HelloWorldResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.bundles.AssetsBundle;
import com.yammer.dropwizard.config.Environment;

import static com.yammer.metrics.aop.Instrumentation.instrument;

public class HelloWorldService extends Service<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldService().run(args);
    }

    private HelloWorldService() {
        super("hello-world");
        addCommand(new RenderCommand());
        addBundle(new AssetsBundle());
    }

    @Override
    protected void initialize(HelloWorldConfiguration configuration,
                              Environment environment) {
        final Template template = configuration.buildTemplate();

        environment.addHealthCheck(new TemplateHealthCheck(template));
        environment.addResource(instrument(new HelloWorldResource(template)));
    }

}
