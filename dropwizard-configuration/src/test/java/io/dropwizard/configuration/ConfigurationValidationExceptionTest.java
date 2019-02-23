package io.dropwizard.configuration;

import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

public class ConfigurationValidationExceptionTest {
    private static class Example {
        @NotNull
        @Nullable // Weird combination, but Hibernate Validator is not good with the compile nullable checks
        String woo;
    }

    private ConfigurationValidationException e;

    @Before
    public void setUp() throws Exception {
        assumeThat(Locale.getDefault().getLanguage()).isEqualTo("en");

        final Validator validator = BaseValidator.newValidator();
        final Set<ConstraintViolation<Example>> violations = validator.validate(new Example());
        this.e = new ConfigurationValidationException("config.yml", violations);
    }

    @Test
    public void formatsTheViolationsIntoAHumanReadableMessage() {
        assertThat(e.getMessage())
                .isEqualTo(String.format(
                        "config.yml has an error:%n" +
                                "  * woo must not be null%n"
                ));
    }

    @Test
    public void retainsTheSetOfExceptions() {
        assertThat(e.getConstraintViolations())
                .isNotEmpty();
    }
}
