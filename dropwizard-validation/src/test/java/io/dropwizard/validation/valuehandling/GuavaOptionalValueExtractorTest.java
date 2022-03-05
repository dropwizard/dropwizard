package io.dropwizard.validation.valuehandling;

import com.google.common.base.Optional;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.valueextraction.Unwrapping;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GuavaOptionalValueExtractorTest {

    public static class Example {

        @Min(value = 3, payload = Unwrapping.Unwrap.class)
        Optional<Integer> three = Optional.absent();

        @NotNull(payload = Unwrapping.Unwrap.class)
        Optional<Integer> notNull = Optional.of(123);
    }

    private final Validator validator = Validation
            .byProvider(HibernateValidator.class)
            .configure()
            .addValueExtractor(GuavaOptionalValueExtractor.DESCRIPTOR.getValueExtractor())
            .buildValidatorFactory()
            .getValidator();

    @Test
    void succeedsWhenAbsent() {
        Example example = new Example();
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).isEmpty();
    }

    @Test
    void failsWhenFailingConstraint() {
        Example example = new Example();
        example.three = Optional.of(2);
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).hasSize(1);
    }

    @Test
    void succeedsWhenConstraintsMet() {
        Example example = new Example();
        example.three = Optional.of(10);
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).isEmpty();
    }

    @Test
    void notNullFailsWhenAbsent() {
        Example example = new Example();
        example.notNull = Optional.absent();
        Set<ConstraintViolation<Example>> violations = validator.validate(example);
        assertThat(violations).hasSize(1);
    }
}
