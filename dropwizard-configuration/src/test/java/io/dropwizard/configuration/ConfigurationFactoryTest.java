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
import java.util.List;
import java.util.Locale;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class ConfigurationFactoryTest {
    @SuppressWarnings("UnusedDeclaration")
    public static class Example {

        @NotNull
        @Pattern(regexp = "[\\w]+[\\s]+[\\w]+([\\s][\\w]+)?")
        private String name;

        @JsonProperty
        private int age = 1;
        
        List<String> type;

        public String getName() {
            return name;
        }
        
        public List<String> getType() {
            return type;
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
        assertThat(example.getType().get(0))
        .isEqualTo("coder");
        assertThat(example.getType().get(1))
        .isEqualTo("wizard");
    }
    
    @Test
    public void handlesSimpleOverride() throws Exception {
        try
        {
            System.setProperty("dw.name", "Coda Hale Overridden");
            final Example example = factory.build(validFile);
            assertThat(example.getName())
                .isEqualTo("Coda Hale Overridden");
        } finally {
            System.clearProperty("dw.name");
        }
    }
    
    @Test
    public void handlesArrayOverride() throws Exception {
        try
        {
            System.setProperty("dw.type", "coder|wizard|overridden");
            final Example example = factory.build(validFile);
            assertThat(example.getType().get(2))
            .isEqualTo("overridden");
            assertThat(example.getType().size())
            .isEqualTo(3);
        } finally {
            System.clearProperty("dw.type");
        }
    }
    @Test
    public void handlesSingleElementArrayOverride() throws Exception {
        try
        {
            System.setProperty("dw.type", "overridden");
            final Example example = factory.build(validFile);
            assertThat(example.getType().get(0))
            .isEqualTo("overridden");
            assertThat(example.getType().size())
            .isEqualTo(1);
        } finally {
            System.clearProperty("dw.type");
        }
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
                                        "  * name must match \"[\\w]+[\\s]+[\\w]+([\\s][\\w]+)?\" (was Boop)%n"));
            }
        }
    }
}
