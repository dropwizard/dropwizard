package io.dropwizard.validation;

import io.dropwizard.Bundle;
import io.dropwizard.jersey.validation.MutableValidatorFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorFactory;
import javax.ws.rs.container.ResourceContext;

/**
 * Dropwizard Bundle that enables injecting into constraint validators
 */
public class InjectValidatorBundle implements Bundle {

    @Nullable
    private MutableValidatorFactory mutableValidatorFactory;

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        ConstraintValidatorFactory factory = bootstrap
            .getValidatorFactory()
            .getConstraintValidatorFactory();

        if (factory instanceof MutableValidatorFactory) {
            this.mutableValidatorFactory = (MutableValidatorFactory) factory;
        }
    }

    @Override
    public void run(Environment environment) {
        GetResourceContextFeature getResourceContext = new GetResourceContextFeature(this::setValidatorFactory);
        environment.jersey().register(getResourceContext);
    }

    private void setValidatorFactory(ResourceContext resourceContext) {
        if (mutableValidatorFactory == null) {
            return;
        }

        // Get original Jersey's ConstraintValidatorFactory
        ConstraintValidatorFactory validatorFactory = resourceContext
            .getResource(InjectingConstraintValidatorFactory.class);

        mutableValidatorFactory.setValidatorFactory(validatorFactory);
    }
}
