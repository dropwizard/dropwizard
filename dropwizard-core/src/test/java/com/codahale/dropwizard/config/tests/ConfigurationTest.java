package com.codahale.dropwizard.config.tests;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.logging.LoggingOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.service.ServiceFinder;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConfigurationTest {
    private final Configuration configuration = new Configuration();

    @Test
    public void hasAnHttpConfiguration() throws Exception {
        assertThat(configuration.getServerConfiguration())
                .isNotNull();
    }

    @Test
    public void hasALoggingConfiguration() throws Exception {
        assertThat(configuration.getLoggingConfiguration())
                .isNotNull();
    }

    @Test
    public void ensureConfigSerializable() throws Exception {
        final ObjectMapper mapper = Jackson.newObjectMapper();
        mapper.getSubtypeResolver()
              .registerSubtypes(ServiceFinder.find(LoggingOutput.class).toClassArray());

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
