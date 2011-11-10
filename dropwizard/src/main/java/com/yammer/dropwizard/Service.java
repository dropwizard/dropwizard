package com.yammer.dropwizard;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.modules.JavaModule;

public abstract class Service<T extends Configuration> extends AbstractService<T> {
    protected Service(String name) {
        super(name);
        addModule(new JavaModule());
    }

    @Override
    protected final void subclassServiceInsteadOfThis() {

    }
}
