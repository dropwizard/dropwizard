package io.dropwizard.jersey.validation;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Configuration;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Register a Dropwizard configured {@link Validator} with Jersey, so that Jersey doesn't use its
 * default, which doesn't have our configurations applied.
 */
public class HibernateValidationFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(DefaultConfigurationProvider.class, Singleton.class).to(Configuration.class).in(Singleton.class);
                bindFactory(DefaultValidatorFactoryProvider.class, Singleton.class).to(ValidatorFactory.class).in(Singleton.class);
                bindFactory(DefaultValidatorProvider.class, Singleton.class).to(Validator.class).in(Singleton.class);
                bindFactory(ConfiguredValidatorProvider.class, Singleton.class).to(ConfiguredValidator.class).in(Singleton.class);
            }
        });

        return true;
    }

    private static class DefaultConfigurationProvider implements Factory<Configuration> {
        @Override
        public Configuration provide() {
            return Validators.newConfiguration();
        }

        @Override
        public void dispose(final Configuration configuration) {
        }
    }

    /**
     * Factory providing default (un-configured) {@link ValidatorFactory} instance.
     */
    private static class DefaultValidatorFactoryProvider implements Factory<ValidatorFactory> {

        @Inject
        private Configuration config;

        @Override
        public ValidatorFactory provide() {
            return config.buildValidatorFactory();
        }

        @Override
        public void dispose(final ValidatorFactory instance) {
            // NOOP
        }
    }

    /**
     * Factory providing default (un-configured) {@link Validator} instance.
     */
    private static class DefaultValidatorProvider implements Factory<Validator> {

        @Inject
        private ValidatorFactory factory;

        @Override
        public Validator provide() {
            return factory.getValidator();
        }

        @Override
        public void dispose(final Validator instance) {
            // NOOP
        }
    }

    private static class ConfiguredValidatorProvider implements Factory<ConfiguredValidator> {
        @Inject
        private ValidatorFactory factory;

        @Override
        public ConfiguredValidator provide() {
            return new DropwizardConfiguredValidator(factory.getValidator());
        }

        @Override
        public void dispose(ConfiguredValidator configuredValidator) {

        }
    }
}
