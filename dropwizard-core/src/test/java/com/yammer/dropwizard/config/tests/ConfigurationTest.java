package com.yammer.dropwizard.config.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConfigurationTest {
    private final Configuration configuration = new Configuration();

    @Test
    public void hasAnHttpConfiguration() throws Exception {
        assertThat(configuration.getHttpConfiguration())
                .isNotNull();
    }

    @Test
    public void hasALoggingConfiguration() throws Exception {
        assertThat(configuration.getLoggingConfiguration())
                .isNotNull();
    }

    @Test
    public void ensureConfigSerializable() throws Exception {
        final ObjectMapper mapper = new ObjectMapperFactory().build();

        // Issue-96: some types were not serializable
        final String json = mapper.writeValueAsString(configuration);
        assertThat(json)
                .isNotNull();

        // and as an added bonus, let's see we can also read it back:
        final Configuration cfg = mapper.readValue(json, Configuration.class);
        assertThat(cfg)
                .isNotNull();
    }
}
