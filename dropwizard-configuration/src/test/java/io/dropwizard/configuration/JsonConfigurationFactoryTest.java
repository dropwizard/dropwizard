package io.dropwizard.configuration;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonConfigurationFactoryTest extends BaseConfigurationFactoryTest {

    private String commentFile;

    @BeforeEach
    void setUp() {
        this.factory = new JsonConfigurationFactory<>(Example.class, validator, newObjectMapper(), "dw");
        this.malformedFile = "factory-test-malformed.json";
        this.malformedFileError = "* Malformed JSON at line:";
        this.emptyFile = "factory-test-empty.json";
        this.invalidFile = "factory-test-invalid.json";
        this.validFile = "factory-test-valid.json";
        this.validNoTypeFile = "factory-test-valid-no-type.json";
        this.commentFile = "factory-test-comment.json";
        this.typoFile = "factory-test-typo.json";
        this.wrongTypeFile = "factory-test-wrong-type.json";
        this.malformedAdvancedFile = "factory-test-malformed-advanced.json";
        this.malformedAdvancedFileError = String.format("%s has an error:%n" +
                "  * Malformed JSON at line: 7, column: 2; Unexpected close marker '}': expected ']'", malformedAdvancedFile);
    }

    @Test
    void defaultJsonFactoryFailsOnComment() {
        assertThatThrownBy(() -> factory.build(configurationSourceProvider, commentFile))
                .hasMessageContaining(String.format(
                        "%s has an error:%n" +
                        "  * Malformed JSON at line: 4, column: 3; Unexpected character ('/' (code 47)): maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)",
                    commentFile));
    }

    @Test
    void configuredMapperAllowsComment() throws IOException, ConfigurationException {
        ObjectMapper mapper = newObjectMapper()
            .configure(Feature.ALLOW_COMMENTS, true);

        JsonConfigurationFactory<Example> factory = new JsonConfigurationFactory<>(Example.class, validator, mapper, "dw");
        assertThat(factory.build(configurationSourceProvider, commentFile).getName())
            .isEqualTo("Mighty Wizard commentator");
    }
}
