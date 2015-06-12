package io.dropwizard.jersey.validation;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import javax.validation.Configuration;
import javax.validation.Validator;
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
}
