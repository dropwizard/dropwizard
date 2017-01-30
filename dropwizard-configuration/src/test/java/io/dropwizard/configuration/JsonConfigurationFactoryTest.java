package io.dropwizard.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.jackson.Jackson;

public class JsonConfigurationFactoryTest extends BaseConfigurationFactoryTest {

    @Override
    public void setUp() throws Exception {
        this.factory = new JsonConfigurationFactory<>(Example.class, validator, Jackson.newObjectMapper(), "dw");
        this.malformedFile = resourceFileName("factory-test-malformed.json");
        this.emptyFile = resourceFileName("factory-test-empty.json");
        this.invalidFile = resourceFileName("factory-test-invalid.json");
        this.validFile = resourceFileName("factory-test-valid.json");
        this.typoFile = resourceFileName("factory-test-typo.json");
        this.wrongTypeFile = resourceFileName("factory-test-wrong-type.json");
        this.malformedAdvancedFile = resourceFileName("factory-test-malformed-advanced.json");
    }
    
    @Override
    public void throwsAnExceptionOnMalformedFiles() throws Exception {
        try {
            super.throwsAnExceptionOnMalformedFiles();
        } catch (ConfigurationParsingException e) {
            assertThat(e)
                    .hasMessageContaining("* Malformed JSON at line:");
        }
    }

    @Override
    public void printsDetailedInformationOnMalformedContent() throws Exception {
        try {
            super.printsDetailedInformationOnMalformedContent();
        } catch (ConfigurationParsingException e) {
            assertThat(e)
                    .hasMessageContaining(String.format(
                            "%s has an error:%n" +
                                    "  * Malformed JSON at line: 7, column: 3; Unexpected close marker '}': expected ']'",
                                    malformedAdvancedFile.getName()));
        }
    }
}