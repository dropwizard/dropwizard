package io.dropwizard.jersey.validation;

import io.dropwizard.validation.BaseValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * A utility class for Hibernate.
 */
public class Validators {
    private Validators() { /* singleton */ }

    /**
     * Creates a new {@link Validator} based on {@link #newValidatorFactory()}
     */
    public static Validator newValidator() {
        return newValidatorFactory().getValidator();
    }

    /**
     * Creates a new {@link ValidatorFactory} based on {@link #newConfiguration()}
     */
    public static ValidatorFactory newValidatorFactory() {
        return newConfiguration().buildValidatorFactory();
    }

    /**
     * Creates a new {@link HibernateValidatorConfiguration} with all the custom value extractors registered.
     */
    public static HibernateValidatorConfiguration newConfiguration() {
        return BaseValidator.newConfiguration()
                .constraintValidatorFactory(new MutableValidatorFactory())
                .parameterNameProvider(new JerseyParameterNameProvider())
                .addValueExtractor(NonEmptyStringParamValueExtractor.DESCRIPTOR.getValueExtractor())
                .addValueExtractor(ParamValueExtractor.DESCRIPTOR.getValueExtractor());
    }
}
