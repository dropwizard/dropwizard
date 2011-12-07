package com.yammer.dropwizard.freemarker.example;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.bundles.TemplateBundle;

public class TemplateService extends Service<Configuration> {
    public static void main(String[] args) throws Exception {
        new TemplateService().run(args);
    }

    private TemplateService() {
        super("template");
        addBundle(new TemplateBundle());
    }

    @Override
    protected void initialize(Configuration configuration, Environment environment) {
        environment.addResource(new TemplateResource());
    }
}
