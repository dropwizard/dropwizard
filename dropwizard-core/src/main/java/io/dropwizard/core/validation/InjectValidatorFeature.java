package io.dropwizard.core.validation;

import io.dropwizard.jersey.validation.MutableValidatorFactory;
import javax.inject.Inject;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ValidatorFactory;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

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
