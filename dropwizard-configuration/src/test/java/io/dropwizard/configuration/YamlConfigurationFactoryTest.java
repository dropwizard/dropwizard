package io.dropwizard.configuration;

import io.dropwizard.jackson.Jackson;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class YamlConfigurationFactoryTest extends BaseConfigurationFactoryTest {

    @Override
    public void setUp() throws Exception {
        this.factory = new YamlConfigurationFactory<>(Example.class, validator, Jackson.newObjectMapper(), "dw");
        this.malformedFile = resourceFileName("factory-test-malformed.yml");
        this.emptyFile = resourceFileName("factory-test-empty.yml");
        this.invalidFile = resourceFileName("factory-test-invalid.yml");
        this.validFile = resourceFileName("factory-test-valid.yml");
        this.typoFile = resourceFileName("factory-test-typo.yml");
        this.wrongTypeFile = resourceFileName("factory-test-wrong-type.yml");
        this.malformedAdvancedFile = resourceFileName("factory-test-malformed-advanced.yml");
    }

    @Override
    public void throwsAnExceptionOnMalformedFiles() {
        assertThatThrownBy(super::throwsAnExceptionOnMalformedFiles)
            .hasMessageContaining(" * Failed to parse configuration; Cannot construct instance of `io.dropwizard.configuration.BaseConfigurationFactoryTest$Example`");
    }

    @Override
    public void printsDetailedInformationOnMalformedContent() throws Exception {
        assertThatThrownBy(super::printsDetailedInformationOnMalformedContent)
            .hasMessageContaining(String.format(
                "%s has an error:%n" +
                "  * Malformed YAML at line: 3, column: 22; while parsing a flow sequence\n" +
                " in 'reader', line 2, column 7:\n" +
                "    type: [ coder,wizard\n" +
                "          ^\n" +
                "expected ',' or ']', but got StreamEnd\n" +
                " in 'reader', line 2, column 21:\n" +
                "    wizard\n" +
                "          ^", malformedAdvancedFile.getName()));
    }
}
