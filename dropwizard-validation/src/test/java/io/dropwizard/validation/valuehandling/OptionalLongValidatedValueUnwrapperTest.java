package io.dropwizard.validation.valuehandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.OptionalLong;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidator;
import org.junit.Test;

public class OptionalLongValidatedValueUnwrapperTest {

    public static class Example {
        @Min(3)
        //@UnwrapValidatedValue
        public OptionalLong three = OptionalLong.empty();

        @NotNull
        //@UnwrapValidatedValue
        public OptionalLong notNull = OptionalLong.of(123456789L);
    }

	private final Validator validator = Validation.byProvider(HibernateValidator.class).configure()
			.buildValidatorFactory().getValidator();

    @Test
    public void succeedsWhenAbsent() {
        Example example = new Example();
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).isEmpty();
    }

    @Test
    public void failsWhenFailingConstraint() {
        Example example = new Example();
        example.three = OptionalLong.of(2);
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).hasSize(1);
    }

    @Test
    public void succeedsWhenConstraintsMet() {
        Example example = new Example();
        example.three = OptionalLong.of(10);
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).isEmpty();
    }

    @Test
    public void notNullFailsWhenAbsent() {
        Example example = new Example();
        example.notNull = OptionalLong.empty();
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).hasSize(1);
    }
}
