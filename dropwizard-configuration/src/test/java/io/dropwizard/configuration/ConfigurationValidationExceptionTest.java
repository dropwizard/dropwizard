package io.dropwizard.configuration;

import io.dropwizard.validation.BaseValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIf("isDefaultLocaleEnglish")
class ConfigurationValidationExceptionTest {
    private static class Example {
        @NotNull
        @Nullable // Weird combination, but Hibernate Validator is not good with the compile nullable checks
        String woo;
    }

    private ConfigurationValidationException e;

    @BeforeEach
    void setUp() {
        final Validator validator = BaseValidator.newValidator();
        final Set<ConstraintViolation<Example>> violations = validator.validate(new Example());
        this.e = new ConfigurationValidationException("config.yml", violations);
    }

    @Test
    void formatsTheViolationsIntoAHumanReadableMessage() {
        assertThat(e.getMessage())
            .isEqualToNormalizingNewlines(
                "config.yml has an error:\n" +
                "  * woo must not be null\n");
    }

    @Test
    void retainsTheSetOfExceptions() {
        assertThat(e.getConstraintViolations())
                .isNotEmpty();
    }

    private static boolean isDefaultLocaleEnglish() {
        return "en".equals(Locale.getDefault().getLanguage());
    }
}
