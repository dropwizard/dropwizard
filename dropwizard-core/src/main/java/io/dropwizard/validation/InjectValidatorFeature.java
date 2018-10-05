package io.dropwizard.validation;

import io.dropwizard.jersey.validation.MutableValidatorFactory;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ValidatorFactory;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class InjectValidatorFeature implements Feature {

    private final ValidatorFactory validatorFactory;

    @Inject
    private ResourceContext resourceContext;

    public InjectValidatorFeature(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Override
    public boolean configure(FeatureContext context) {
        ConstraintValidatorFactory constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
        if (constraintValidatorFactory instanceof MutableValidatorFactory) {
            MutableValidatorFactory mutableValidatorFactory = (MutableValidatorFactory) constraintValidatorFactory;
            ConstraintValidatorFactory validatorFactory =
                resourceContext.getResource(InjectingConstraintValidatorFactory.class);

            mutableValidatorFactory.setValidatorFactory(validatorFactory);
            return true;
        }

        return false;
    }
}
