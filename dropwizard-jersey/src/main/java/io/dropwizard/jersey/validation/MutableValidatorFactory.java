package io.dropwizard.jersey.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validation;
import org.hibernate.validator.HibernateValidator;

/**
 * @since 2.0
 */
public class MutableValidatorFactory implements ConstraintValidatorFactory {

    private ConstraintValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class).configure().getDefaultConstraintValidatorFactory();

    @Override
    public final <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        return validatorFactory.getInstance(key);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {
        // Nothing to do
    }

    public void setValidatorFactory(ConstraintValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }
}
