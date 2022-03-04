package io.dropwizard.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.core.Configuration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.logging.AppenderFactory;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {
    private final Configuration configuration = new Configuration();

    @Test
    void hasAnHttpConfiguration() throws Exception {
        assertThat(configuration.getServerFactory())
                .isNotNull();
    }

    @Test
    void hasALoggingConfiguration() throws Exception {
        assertThat(configuration.getLoggingFactory())
                .isNotNull();
    }

    @Test
    void ensureConfigSerializable() throws Exception {
        final ObjectMapper mapper = Jackson.newObjectMapper();
        Class<?>[] dummyArray = {};

        mapper.getSubtypeResolver()
            .registerSubtypes(StreamSupport.stream(ServiceLoader.load(AppenderFactory.class).spliterator(), false)
                .map(Object::getClass)
                .collect(Collectors.toList())
                .toArray(dummyArray));

        mapper.getSubtypeResolver()
            .registerSubtypes(StreamSupport.stream(ServiceLoader.load(ConnectorFactory.class).spliterator(), false)
                .map(Object::getClass)
                .collect(Collectors.toList())
                .toArray(dummyArray));

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
