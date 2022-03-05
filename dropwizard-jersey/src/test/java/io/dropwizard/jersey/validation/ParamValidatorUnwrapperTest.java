package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.NonEmptyStringParam;
import org.hibernate.validator.constraints.Length;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.valueextraction.Unwrapping;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ParamValidatorUnwrapperTest {
    private static class Example {
        @NotNull(payload = Unwrapping.Skip.class)
        @Min(3)
        IntParam inter = new IntParam("4");

        @NotNull(payload = Unwrapping.Skip.class)
        @NotEmpty
        @Length(max = 3)
        public NonEmptyStringParam name = new NonEmptyStringParam("a");
    }

    private final Validator validator = Validators.newValidator();

    @Test
    void succeedsWithAllGoodData() {
        final Example example = new Example();
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).isEmpty();
    }

    @Test
    void failsWithInvalidIntParam() {
        final Example example = new Example();
        example.inter = new IntParam("2");
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }

    @SuppressWarnings("NullAway")
    @Test
    void failsWithNullIntParam() {
        final Example example = new Example();
        example.inter = null;
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }

    @SuppressWarnings("NullAway")
    @Test
    void failsWithNullNonEmptyStringParam() {
        final Example example = new Example();
        example.name = null;
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }

    @Test
    void failsWithInvalidNonEmptyStringParam() {
        final Example example = new Example();
        example.name = new NonEmptyStringParam("hello");
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }

    @Test
    void failsWithEmptyNonEmptyStringParam() {
        final Example example = new Example();
        example.name = new NonEmptyStringParam("");
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }
}
