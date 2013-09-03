package com.codahale.dropwizard.server;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.jackson.Jackson; 
import com.codahale.dropwizard.logging.ConsoleAppenderFactory;
import com.codahale.dropwizard.logging.FileAppenderFactory;
import com.codahale.dropwizard.logging.SyslogAppenderFactory;
import com.codahale.dropwizard.server.ServerConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper; 
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConfigurationTest {
    private final ServerConfiguration configuration = new ServerConfiguration();

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
              .registerSubtypes(new Class[]{SyslogAppenderFactory.class,
                  ConsoleAppenderFactory.class,
                  FileAppenderFactory.class});

        // Issue-96: some types were not serializable
        final String json = mapper.writeValueAsString(configuration);
        assertThat(json)
                .isNotNull();

        // and as an added bonus, let's see we can also read it back:
        final Configuration cfg = mapper.readValue(json, ServerConfiguration.class);
        assertThat(cfg)
                .isNotNull();
    }
}
