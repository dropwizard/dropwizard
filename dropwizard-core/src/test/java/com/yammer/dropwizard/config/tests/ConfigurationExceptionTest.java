package com.yammer.dropwizard.config.tests;

import com.yammer.dropwizard.config.ConfigurationException;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationExceptionTest {
    public static class ValidationExample {
        @NotNull
        @SuppressWarnings("UnusedDeclaration")
        private String woo = null;
    }

    @Test
    public void formatsTheViolationsIntoAHumanReadableMessage() throws Exception {
        final File file = new File("config.yml");
        final Set<ConstraintViolation<ValidationExample>> violations = Validation.buildDefaultValidatorFactory()
                                                                                 .getValidator()
                                                                                 .validate(new ValidationExample());
        final ConfigurationException e = new ConfigurationException(file, violations);

        assertThat(e.getMessage(),
                   is("config.yml has the following errors:\n" +
                      "  * woo may not be null"));
    }
}
