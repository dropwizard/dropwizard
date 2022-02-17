package io.dropwizard.validation;

import io.dropwizard.jersey.validation.MutableValidatorFactory;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

/**
 * @since 2.0
 */
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
            ConstraintValidatorFactory resourceContextValidatorFactory =
                resourceContext.getResource(InjectingConstraintValidatorFactory.class);

            mutableValidatorFactory.setValidatorFactory(resourceContextValidatorFactory);
            return true;
        }

        return false;
    }
}
