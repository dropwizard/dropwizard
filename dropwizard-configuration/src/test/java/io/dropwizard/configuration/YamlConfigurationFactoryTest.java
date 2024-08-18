package io.dropwizard.configuration;

import org.junit.jupiter.api.BeforeEach;

import static io.dropwizard.jackson.Jackson.newObjectMapper;

public class YamlConfigurationFactoryTest extends BaseConfigurationFactoryTest {

    @BeforeEach
    public void setUp() throws Exception {
        this.factory = new YamlConfigurationFactory<>(Example.class, validator, newObjectMapper(), "dw");
        this.malformedFile = "factory-test-malformed.yml";
        this.malformedFileError = " * Failed to parse configuration; Cannot construct instance of `io.dropwizard.configuration.BaseConfigurationFactoryTest$Example`";
        this.emptyFile = "factory-test-empty.yml";
        this.invalidFile = "factory-test-invalid.yml";
        this.validFile = "factory-test-valid.yml";
        this.validNoTypeFile = "factory-test-valid-no-type.yml";
        this.typoFile = "factory-test-typo.yml";
        this.wrongTypeFile = "factory-test-wrong-type.yml";
        this.malformedAdvancedFile = "factory-test-malformed-advanced.txt";
        this.malformedAdvancedFileError = String.format("%s has an error:%n" +
            "  * Malformed YAML at line: 3, column: 21; while parsing a flow sequence\n" +
            " in 'reader'", malformedAdvancedFile);
    }
}
