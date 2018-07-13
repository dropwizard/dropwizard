package io.dropwizard.jersey.validation;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

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
