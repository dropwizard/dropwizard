package io.dropwizard.validation;

import io.dropwizard.Bundle;
import io.dropwizard.jersey.validation.MutableValidatorFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ValidatorFactory;
import javax.ws.rs.container.ResourceContext;

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
        GetLocatorFeature getLocatorFeature = new GetLocatorFeature(this::setValidatorFactory);
        environment.jersey().register(getLocatorFeature);
    }

    private void setValidatorFactory(ServiceLocator serviceLocator) {
        if (mutableValidatorFactory == null) {
            return;
        }

        ConstraintValidatorFactory validatorFactory = serviceLocator
            .getService(ResourceContext.class)
            .getResource(InjectingConstraintValidatorFactory.class);

        mutableValidatorFactory.setValidatorFactory(validatorFactory);
    }

}
