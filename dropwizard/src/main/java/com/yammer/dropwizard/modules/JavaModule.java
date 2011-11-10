package com.yammer.dropwizard.modules;

import com.yammer.dropwizard.Module;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;

public class JavaModule implements Module {
    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new JacksonMessageBodyProvider());
    }
}
