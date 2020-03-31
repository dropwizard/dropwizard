package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.NonEmptyStringParam;
import org.hibernate.validator.constraints.Length;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.valueextraction.Unwrapping;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ParamValidatorUnwrapperTest {
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
    public void succeedsWithAllGoodData() {
        final Example example = new Example();
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).isEmpty();
    }

    @Test
    public void failsWithInvalidIntParam() {
        final Example example = new Example();
        example.inter = new IntParam("2");
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }

    @SuppressWarnings("NullAway")
    @Test
    public void failsWithNullIntParam() {
        final Example example = new Example();
        example.inter = null;
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }

    @SuppressWarnings("NullAway")
    @Test
    public void failsWithNullNonEmptyStringParam() {
        final Example example = new Example();
        example.name = null;
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }

    @Test
    public void failsWithInvalidNonEmptyStringParam() {
        final Example example = new Example();
        example.name = new NonEmptyStringParam("hello");
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }

    @Test
    public void failsWithEmptyNonEmptyStringParam() {
        final Example example = new Example();
        example.name = new NonEmptyStringParam("");
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }
}