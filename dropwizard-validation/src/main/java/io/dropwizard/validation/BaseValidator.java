package io.dropwizard.validation;

import io.dropwizard.validation.valuehandling.GuavaOptionalValueExtractor;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import javax.validation.Validation;
import javax.validation.Validator;

/**
 * This class provides static methods to obtain configured {@link Validator} instances.
 */
public class BaseValidator {
    private BaseValidator() { /* singleton */ }

    /**
     * Creates a new {@link Validator} based on {@link #newConfiguration()}.
     *
     * @return the new {@link Validator} instance
     */
    public static Validator newValidator() {
        return newConfiguration().buildValidatorFactory().getValidator();
    }

    /**
     * Creates a new {@link HibernateValidatorConfiguration} with the base custom unwrappers registered.
     *
     * @return the configured {@link HibernateValidatorConfiguration}
     */
    public static HibernateValidatorConfiguration newConfiguration() {
        return Validation
            .byProvider(HibernateValidator.class)
            .configure()
            .addValueExtractor(GuavaOptionalValueExtractor.DESCRIPTOR.getValueExtractor());
    }
}
