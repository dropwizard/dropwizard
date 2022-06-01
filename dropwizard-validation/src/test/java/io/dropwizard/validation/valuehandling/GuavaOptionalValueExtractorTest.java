package io.dropwizard.validation.valuehandling;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.valueextraction.Unwrapping;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.Test;

class GuavaOptionalValueExtractorTest {

    public static class Example {

        @Min(value = 3, payload = Unwrapping.Unwrap.class)
        Optional<Integer> three = Optional.absent();

        @NotNull(payload = Unwrapping.Unwrap.class)
        Optional<Integer> notNull = Optional.of(123);
    }

    private final Validator validator = Validation.byProvider(HibernateValidator.class)
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
