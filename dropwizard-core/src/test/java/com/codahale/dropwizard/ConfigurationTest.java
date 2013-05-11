package com.codahale.dropwizard;

import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.jetty.ConnectorFactory;
import com.codahale.dropwizard.logging.AppenderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.service.ServiceFinder;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConfigurationTest {
    private final Configuration configuration = new Configuration();

    @Test
    public void hasAnHttpConfiguration() throws Exception {
        assertThat(configuration.getServerFactory())
                .isNotNull();
    }

    @Test
    public void hasALoggingConfiguration() throws Exception {
        assertThat(configuration.getLoggingFactory())
                .isNotNull();
    }

    @Test
    public void ensureConfigSerializable() throws Exception {
        final ObjectMapper mapper = Jackson.newObjectMapper();
        mapper.getSubtypeResolver()
              .registerSubtypes(ServiceFinder.find(AppenderFactory.class).toClassArray());
        mapper.getSubtypeResolver()
              .registerSubtypes(ServiceFinder.find(ConnectorFactory.class).toClassArray());

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
