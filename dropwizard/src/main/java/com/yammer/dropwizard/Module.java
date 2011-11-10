package com.yammer.dropwizard;

import com.yammer.dropwizard.config.Environment;

public interface Module {
    public void initialize(Environment environment);
}
