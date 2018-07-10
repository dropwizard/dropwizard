package io.dropwizard.jersey.validation;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

import javax.inject.Inject;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

public class HibernateValidationBinder extends AbstractBinder {
    private final ValidatorFactory factory;

    public HibernateValidationBinder(ValidatorFactory factory) {
        this.factory = factory;
    }

    @Override
    protected void configure() {
        bind(factory).to(ValidatorFactory.class);
        bindFactory(ValidatorProvider.class).to(ConfiguredValidator.class);
    }

    private static class ValidatorProvider implements Factory<ConfiguredValidator> {
        @Context
        private ResourceContext resourceContext;

        private final ValidatorFactory factory;

        @Inject
        @SuppressWarnings("NullAway") // NullAway can't prove that resourceContext is not null
        private ValidatorProvider(ValidatorFactory factory) {
            this.factory = factory;
        }

        @Override
        public ConfiguredValidator provide() {
            final ValidatorContext context = factory.usingContext();
            context.constraintValidatorFactory(resourceContext.getResource(InjectingConstraintValidatorFactory.class));
            return new DropwizardConfiguredValidator(context.getValidator());
        }

        @Override
        public void dispose(ConfiguredValidator instance) {
        }
    }
}
