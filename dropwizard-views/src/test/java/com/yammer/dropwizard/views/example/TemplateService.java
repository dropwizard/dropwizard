package com.yammer.dropwizard.views.example;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

public class TemplateService extends Service<Configuration> {
    public static void main(String[] args) throws Exception {
        new TemplateService().run(args);
    }

    private TemplateService() {
        super("template");
        addBundle(new ViewBundle());
    }

    @Override
    protected void run(Configuration configuration, Environment environment) {
        environment.addResource(new HelloWorldResource());
        environment.addResource(new BadResource());
        environment.addResource(new AnotherResource());
    }
}
