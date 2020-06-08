package io.dropwizard.documentation;

import io.dropwizard.Application;
import io.dropwizard.documentation.config.MyConfiguration;
import io.dropwizard.documentation.resources.MyResource;
import io.dropwizard.setup.Environment;

public class MyApplication extends Application<MyConfiguration> {
    @Override
    public void run(MyConfiguration configuration, Environment environment) {
        environment.jersey().register(MyResource.class);
    }
}