package io.dropwizard.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationFactoryJSONTest extends ConfigurationFactoryTest {

    @Before
    public void setUp() throws Exception {
        this.malformedFile = resourceFileName("factory-test-malformed.json");
        this.emptyFile = resourceFileName("factory-test-empty.json");
        this.invalidFile = resourceFileName("factory-test-invalid.json");
        this.validFile = resourceFileName("factory-test-valid.json");
    }
    
    @Override
    public void throwsAnExceptionOnInvalidFiles() throws Exception {
    	try {
            factory.build(invalidFile);
            failBecauseExceptionWasNotThrown(ConfigurationValidationException.class);
        } catch (ConfigurationValidationException e) {
            if ("en".equals(Locale.getDefault().getLanguage())) {
                assertThat(e.getMessage())
                        .endsWith(String.format(
                                "factory-test-invalid.json has an error:%n" +
                                        "  * name must match \"[\\w]+[\\s]+[\\w]+([\\s][\\w]+)?\"%n"));
            }
        }
    }
    
    @Override
    public void throwsAnExceptionOnMalformedFiles() throws Exception {
        try {
            factory.build(malformedFile);
            failBecauseExceptionWasNotThrown(ConfigurationParsingException.class);
        } catch (ConfigurationParsingException e) {
            assertThat(e.getMessage())
                    .containsOnlyOnce("* Malformed JSON at line:");
        }
    }
}
