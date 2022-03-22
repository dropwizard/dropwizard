package io.dropwizard.documentation;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.documentation.config.MyConfiguration;
import io.dropwizard.documentation.resources.MyResource;

public class MyApplication extends Application<MyConfiguration> {
    @Override
    public void run(MyConfiguration configuration, Environment environment) {
        environment.jersey().register(MyResource.class);
    }
}
