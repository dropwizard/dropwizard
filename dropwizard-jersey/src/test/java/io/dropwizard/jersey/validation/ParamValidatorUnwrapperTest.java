package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.NonEmptyStringParam;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ParamValidatorUnwrapperTest {

    public static class Example {
        @Min(3)
        @UnwrapValidatedValue
        public IntParam inter = new IntParam("4");

        @Length(max = 3)
        @UnwrapValidatedValue
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

    @Test
    public void failsWithInvalidNonEmptyStringParam() {
        final Example example = new Example();
        example.name = new NonEmptyStringParam("hello");
        final Set<ConstraintViolation<Example>> validate = validator.validate(example);
        assertThat(validate).hasSize(1);
    }
}