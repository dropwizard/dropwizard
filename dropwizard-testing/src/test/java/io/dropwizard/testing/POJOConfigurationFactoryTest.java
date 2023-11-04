package io.dropwizard.testing;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class POJOConfigurationFactoryTest {

    private POJOConfigurationFactory<TestConfiguration> configurationFactory;
    private TestConfiguration TestConfiguration;

    @BeforeEach
    void setUp() {
        TestConfiguration = new TestConfiguration();
        configurationFactory = new POJOConfigurationFactory<>(TestConfiguration);
    }

    @Test
    void buildWithConfigurationSourceProviderAndPathShouldReturnConfiguration() {
        ConfigurationSourceProvider provider = mock(ConfigurationSourceProvider.class);
        String path = "test.yml";

        TestConfiguration result = configurationFactory.build(provider, path);

        assertEquals(TestConfiguration, result);
    }

    @Test
    void buildWithFileShouldReturnConfiguration() {
        File file = new File("test.yml");

        TestConfiguration result = configurationFactory.build(file);

        assertEquals(TestConfiguration, result);
    }

    @Test
    void buildWithoutArgumentsShouldReturnConfiguration() {
        TestConfiguration result = configurationFactory.build();

        assertEquals(TestConfiguration, result);
    }

    @Test
    void buildWithJsonNodeAndPathShouldReturnConfiguration() {
        JsonNode jsonNode = Jackson.newObjectMapper().createObjectNode();
        String path = "test.json";

        TestConfiguration result = configurationFactory.build(jsonNode, path);

        assertEquals(TestConfiguration, result);
    }
}
