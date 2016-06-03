package io.dropwizard.validation.valuehandling;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Dropwizard used to supply its own Java 8 optional validator but since
// Hibernate Validator 5.2, it's built in, so the class was removed but
// the test class stays to ensure behavior remains
public class OptionalValidatedValueUnwrapperTest {

    public static class Example {

        @Min(3)
        @UnwrapValidatedValue
        public Optional<Integer> three = Optional.empty();

        @NotNull
        @UnwrapValidatedValue
        public Optional<Integer> notNull = Optional.of(123);
    }

    private final Validator validator = Validation
            .byProvider(HibernateValidator.class)
            .configure()
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
        example.three = Optional.of(2);
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).hasSize(1);
    }

    @Test
    public void succeedsWhenConstraintsMet() {
        Example example = new Example();
        example.three = Optional.of(10);
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).isEmpty();
    }

    @Test
    public void notNullFailsWhenAbsent() {
        Example example = new Example();
        example.notNull = Optional.empty();
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).hasSize(1);
    }
}
