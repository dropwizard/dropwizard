package io.dropwizard.validation;

import io.dropwizard.validation.valuehandling.GuavaOptionalValueExtractor;
import javax.validation.Validation;
import javax.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

public class BaseValidator {
    private BaseValidator() {
        /* singleton */
    }

    /**
     * Creates a new {@link Validator} based on {@link #newConfiguration()}
     */
    public static Validator newValidator() {
        return newConfiguration().buildValidatorFactory().getValidator();
    }

    /**
     * Creates a new {@link HibernateValidatorConfiguration} with the base custom unwrappers registered.
     */
    public static HibernateValidatorConfiguration newConfiguration() {
        return Validation.byProvider(HibernateValidator.class)
                .configure()
                .addValueExtractor(GuavaOptionalValueExtractor.DESCRIPTOR.getValueExtractor());
    }
}
