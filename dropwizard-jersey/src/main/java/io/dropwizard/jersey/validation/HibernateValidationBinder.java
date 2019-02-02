package io.dropwizard.jersey.validation;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;

import javax.validation.Validator;

public class HibernateValidationBinder extends AbstractBinder {
    private final Validator validator;

    public HibernateValidationBinder(Validator validator) {
        this.validator = validator;
    }

    @Override
    protected void configure() {
        bind(new DropwizardConfiguredValidator(validator)).to(ConfiguredValidator.class);
    }
}
