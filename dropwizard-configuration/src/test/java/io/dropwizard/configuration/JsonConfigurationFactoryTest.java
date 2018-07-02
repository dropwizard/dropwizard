package io.dropwizard.configuration;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonConfigurationFactoryTest extends BaseConfigurationFactoryTest {

    private File commentFile;

    @Override
    public void setUp() throws Exception {
        this.factory = new JsonConfigurationFactory<>(Example.class, validator, Jackson.newObjectMapper(), "dw");
        this.malformedFile = resourceFileName("factory-test-malformed.json");
        this.emptyFile = resourceFileName("factory-test-empty.json");
        this.invalidFile = resourceFileName("factory-test-invalid.json");
        this.validFile = resourceFileName("factory-test-valid.json");
        this.commentFile = resourceFileName("factory-test-comment.json");
        this.typoFile = resourceFileName("factory-test-typo.json");
        this.wrongTypeFile = resourceFileName("factory-test-wrong-type.json");
        this.malformedAdvancedFile = resourceFileName("factory-test-malformed-advanced.json");
    }

    @Override
    public void throwsAnExceptionOnMalformedFiles() {
        assertThatThrownBy(super::throwsAnExceptionOnMalformedFiles)
                .hasMessageContaining("* Malformed JSON at line:");
    }

    @Override
    public void printsDetailedInformationOnMalformedContent() {
        assertThatThrownBy(super::printsDetailedInformationOnMalformedContent)
                .isInstanceOf(ConfigurationParsingException.class)
                .hasMessageContaining(String.format(
                        "%s has an error:%n" +
                        "  * Malformed JSON at line: 7, column: 3; Unexpected close marker '}': expected ']'",
                        malformedAdvancedFile.getName()));
    }

    @Test
    public void defaultJsonFactoryFailsOnComment() {
        assertThatThrownBy(() -> factory.build(commentFile))
                .hasMessageContaining(String.format(
                        "%s has an error:%n" +
                        "  * Malformed JSON at line: 4, column: 4; Unexpected character ('/' (code 47)): maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)",
                        commentFile.getName()));
    }

    @Test
    public void configuredMapperAllowsComment() throws IOException, ConfigurationException {
        ObjectMapper mapper = Jackson
            .newObjectMapper()
            .configure(Feature.ALLOW_COMMENTS, true);

        JsonConfigurationFactory<Example> factory = new JsonConfigurationFactory<>(Example.class, validator, mapper, "dw");
        factory.build(commentFile);
    }
}