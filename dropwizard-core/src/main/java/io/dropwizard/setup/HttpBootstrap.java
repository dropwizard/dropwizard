package io.dropwizard.setup;

import io.dropwizard.Application;
import io.dropwizard.HttpConfiguration;
import io.dropwizard.jersey.validation.JerseyValidators;

public class HttpBootstrap<T extends HttpConfiguration> extends Bootstrap<T> {

    public HttpBootstrap(Application<T> application) {
        super(application);
        this.setValidatorFactory(JerseyValidators.newValidatorFactory());
    }

}
