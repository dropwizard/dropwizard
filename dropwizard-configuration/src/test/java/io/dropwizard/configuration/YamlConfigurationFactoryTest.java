package io.dropwizard.configuration;

import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class YamlConfigurationFactoryTest extends BaseConfigurationFactoryTest {

    @BeforeEach
    public void setUp() throws Exception {
        this.factory = new YamlConfigurationFactory<>(Example.class, validator, Jackson.newObjectMapper(), "dw");
        this.malformedFile = "factory-test-malformed.yml";
        this.emptyFile = "factory-test-empty.yml";
        this.invalidFile = "factory-test-invalid.yml";
        this.validFile = "factory-test-valid.yml";
        this.validNoTypeFile = "factory-test-valid-no-type.yml";
        this.typoFile = "factory-test-typo.yml";
        this.wrongTypeFile = "factory-test-wrong-type.yml";
        this.malformedAdvancedFile = "factory-test-malformed-advanced.yml";
    }

    @Override
    public void throwsAnExceptionOnMalformedFiles() {
        assertThatThrownBy(super::throwsAnExceptionOnMalformedFiles)
            .hasMessageContaining(" * Failed to parse configuration; Cannot construct instance of `io.dropwizard.configuration.BaseConfigurationFactoryTest$Example`");
    }

    @Override
    public void printsDetailedInformationOnMalformedContent() {
        assertThatThrownBy(super::printsDetailedInformationOnMalformedContent)
            .hasMessageContaining(String.format(
                "%s has an error:%n" +
                "  * Malformed YAML at line: 3, column: 22; while parsing a flow sequence\n" +
                " in 'reader'", malformedAdvancedFile));
    }
}
