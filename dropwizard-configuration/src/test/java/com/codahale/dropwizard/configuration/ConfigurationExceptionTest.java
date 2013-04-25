package com.codahale.dropwizard.configuration;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConfigurationExceptionTest {
    private static class Example {
        @NotNull
        String woo;
    }

    @Test
    public void formatsTheViolationsIntoAHumanReadableMessage() throws Exception {
        final Set<ConstraintViolation<Example>> violations = Validation.buildDefaultValidatorFactory()
                                                                       .getValidator()
                                                                       .validate(new Example());

        final ConfigurationException e = new ConfigurationException("config.yml", violations);

        assertThat(e.getMessage())
                .isEqualTo("config.yml has the following errors:\n" +
                                   "  * woo may not be null (was null)\n");
    }
}
