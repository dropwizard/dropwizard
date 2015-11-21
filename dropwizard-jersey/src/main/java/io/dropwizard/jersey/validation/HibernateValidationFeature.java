package io.dropwizard.jersey.validation;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;

import javax.validation.Validator;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Register a Dropwizard configured {@link Validator} with Jersey, so that Jersey doesn't use its
 * default, which doesn't have our configurations applied.
 */
public class HibernateValidationFeature implements Feature {
    private final Validator validator;

    public HibernateValidationFeature(Validator validator) {
        this.validator = validator;
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(new DropwizardConfiguredValidator(validator)).to(ConfiguredValidator.class);
            }
        });

        return true;
    }
}
