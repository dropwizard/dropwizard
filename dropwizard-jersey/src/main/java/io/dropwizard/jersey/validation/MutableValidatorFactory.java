package io.dropwizard.jersey.validation;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

/**
 * @since 2.0
 */
public class MutableValidatorFactory implements ConstraintValidatorFactory {

    private ConstraintValidatorFactory validatorFactory = new ConstraintValidatorFactoryImpl();

    @Override
    public final <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        return validatorFactory.getInstance(key);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) { }

    public void setValidatorFactory(ConstraintValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }
}
