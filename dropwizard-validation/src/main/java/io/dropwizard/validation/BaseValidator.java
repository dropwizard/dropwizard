package io.dropwizard.validation;

import io.dropwizard.validation.valuehandling.GuavaOptionalValidatedValueUnwrapper;
import io.dropwizard.validation.valuehandling.OptionalDoubleValidatedValueUnwrapper;
import io.dropwizard.validation.valuehandling.OptionalIntValidatedValueUnwrapper;
import io.dropwizard.validation.valuehandling.OptionalLongValidatedValueUnwrapper;
import io.dropwizard.validation.valuehandling.OptionalValidatedValueUnwrapper;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

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
     * ValidatedValueUnwrapper} registered.
     */
    public static HibernateValidatorConfiguration newConfiguration() {
        return Validation
            .byProvider(HibernateValidator.class)
            .configure()
            .addValidatedValueHandler(new GuavaOptionalValidatedValueUnwrapper())
            .addValidatedValueHandler(new OptionalValidatedValueUnwrapper())
            .addValidatedValueHandler(new OptionalDoubleValidatedValueUnwrapper())
            .addValidatedValueHandler(new OptionalIntValidatedValueUnwrapper())
            .addValidatedValueHandler(new OptionalLongValidatedValueUnwrapper());
    }
}
