package io.dropwizard.validation.valuehandling;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.OptionalInt;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalIntValidatedValueUnwrapperTest {

    public static class Example {
        @Min(3)
        @UnwrapValidatedValue
        public OptionalInt three = OptionalInt.empty();

        @NotNull
        @UnwrapValidatedValue
        public OptionalInt notNull = OptionalInt.of(123);
    }

    private final Validator validator = Validation
        .byProvider(HibernateValidator.class)
        .configure()
        .addValidatedValueHandler(new OptionalIntValidatedValueUnwrapper())
        .buildValidatorFactory()
        .getValidator();

    @Test
    public void succeedsWhenAbsent() {
        Example example = new Example();
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).isEmpty();
    }

    @Test
    public void failsWhenFailingConstraint() {
        Example example = new Example();
        example.three = OptionalInt.of(2);
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).hasSize(1);
    }

    @Test
    public void succeedsWhenConstraintsMet() {
        Example example = new Example();
        example.three = OptionalInt.of(10);
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).isEmpty();
    }

    @Test
    public void notNullFailsWhenAbsent() {
        Example example = new Example();
        example.notNull = OptionalInt.empty();
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).hasSize(1);
    }
}
