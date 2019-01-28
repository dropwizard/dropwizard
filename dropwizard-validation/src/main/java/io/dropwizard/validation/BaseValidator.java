package io.dropwizard.validation;

import io.dropwizard.validation.valuehandling.GuavaOptionalValidatedValueExtractor;
import io.dropwizard.validation.valuehandling.OptionalDoubleValidatedValueExtractor;
import io.dropwizard.validation.valuehandling.OptionalIntValidatedValueExtractor;
import io.dropwizard.validation.valuehandling.OptionalLongValidatedValueExtractor;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import javax.validation.Validation;
import javax.validation.Validator;

public class BaseValidator {
    private BaseValidator() { /* singleton */ }

    /**
     * Creates a new {@link Validator} based on {@link #newConfiguration()}
     */
    public static Validator newValidator() {
        return newConfiguration().buildValidatorFactory().getValidator();
    }

    /**
     * Creates a new {@link HibernateValidatorConfiguration} with the base custom {@link
     * javax.validation.valueextraction.ValueExtractor} registered.
     */
    public static HibernateValidatorConfiguration newConfiguration() {
        return Validation
            .byProvider(HibernateValidator.class)
            .configure()
            .addValueExtractor(new GuavaOptionalValidatedValueExtractor())
            .addValueExtractor(new OptionalDoubleValidatedValueExtractor())
            .addValueExtractor(new OptionalIntValidatedValueExtractor())
            .addValueExtractor(new OptionalLongValidatedValueExtractor());
    }
}
