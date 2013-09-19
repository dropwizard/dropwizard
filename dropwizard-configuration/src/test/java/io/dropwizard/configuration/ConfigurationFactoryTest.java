package io.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.util.Locale;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class ConfigurationFactoryTest {
    @SuppressWarnings("UnusedDeclaration")
    public static class Example {

        @NotNull
        @Pattern(regexp = "[\\w]+[\\s]+[\\w]+")
        private String name;

        @JsonProperty
        private int age = 1;

        public String getName() {
            return name;
        }
    }

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ConfigurationFactory<Example> factory =
            new ConfigurationFactory<>(Example.class, validator, Jackson.newObjectMapper(), "dw");
    private File malformedFile;
    private File invalidFile;
    private File validFile;

    @Before
    public void setUp() throws Exception {
        this.malformedFile = new File(Resources.getResource("factory-test-malformed.yml").toURI());
        this.invalidFile = new File(Resources.getResource("factory-test-invalid.yml").toURI());
        this.validFile = new File(Resources.getResource("factory-test-valid.yml").toURI());
    }

    @Test
    public void loadsValidConfigFiles() throws Exception {
        final Example example = factory.build(validFile);
        assertThat(example.getName())
                .isEqualTo("Coda Hale");
    }

    @Test
    public void throwsAnExceptionOnMalformedFiles() throws Exception {
        try {
            factory.build(malformedFile);
            failBecauseExceptionWasNotThrown(ConfigurationParsingException.class);
        } catch (ConfigurationParsingException e) {
            assertThat(e.getMessage())
                    .containsOnlyOnce(" * Failed to parse configuration; Can not instantiate");
        }
    }

    @Test
    public void throwsAnExceptionOnInvalidFiles() throws Exception {
        try {
            factory.build(invalidFile);
        } catch (ConfigurationValidationException e) {
            if ("en".equals(Locale.getDefault().getLanguage())) {
                assertThat(e.getMessage())
                        .endsWith(String.format(
                                "factory-test-invalid.yml has an error:%n" +
                                        "  * name must match \"[\\w]+[\\s]+[\\w]+\" (was Boop)%n"));
            }
        }
    }
}
