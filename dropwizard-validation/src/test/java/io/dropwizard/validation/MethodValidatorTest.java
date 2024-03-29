package io.dropwizard.validation;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"FieldMayBeFinal", "MethodMayBeStatic", "UnusedDeclaration"})
class MethodValidatorTest {
    public static class SubExample {
        @ValidationMethod(message = "also needs something special")
        public boolean isOK() {
            return false;
        }
    }

    public static class Example {
        @Valid
        private SubExample subExample = new SubExample();

        @ValidationMethod(message = "must have a false thing")
        public boolean isFalse() {
            return false;
        }

        @ValidationMethod(message = "must have a true thing")
        public boolean isTrue() {
            return true;
        }
    }

    private final Validator validator = BaseValidator.newValidator();

    @Test
    void complainsAboutMethodsWhichReturnFalse() throws Exception {
        final Collection<String> errors =
                ConstraintViolations.format(validator.validate(new Example()));

        assertThat(errors)
                .containsOnly("must have a false thing",
                              "also needs something special");
    }
}
