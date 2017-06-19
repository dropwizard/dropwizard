package io.dropwizard.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;

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
    
    @Test(expected = ConfigurationParsingException.class)
    public void defaultJsonFactoryFailsOnComment() throws IOException, ConfigurationException {
        try {
            factory.build(commentFile);
        } catch (ConfigurationParsingException e) {
            assertThat(e)
                .hasMessageContaining(String.format(
                    "%s has an error:%n" +
                        "  * Malformed JSON at line: 4, column: 4; Unexpected character ('/' (code 47)): maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)",
                        commentFile.getName()));
            throw e;
        }
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