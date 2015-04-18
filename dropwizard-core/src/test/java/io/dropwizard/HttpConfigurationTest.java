package io.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.logging.AppenderFactory;
import org.junit.Test;

import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpConfigurationTest {
    private final HttpConfiguration configuration = new HttpConfiguration();

    @Test
    public void hasAnHttpConfiguration() throws Exception {
        assertThat(configuration.getServerFactory())
                .isNotNull();
    }

    @Test
    public void ensureConfigSerializable() throws Exception {
        final ObjectMapper mapper = Jackson.newObjectMapper();
        Class<?>[] dummyArray = {};

        mapper.getSubtypeResolver()
            .registerSubtypes(Lists
                    .newArrayList(Iterables.transform(ServiceLoader.load(AppenderFactory.class),
                            new Function<AppenderFactory,Class<AppenderFactory>> () {
                                @SuppressWarnings("unchecked")
                                public Class<AppenderFactory> apply(AppenderFactory factory) {
                                    return (Class<AppenderFactory>) factory.getClass();
                                }
                            }
                       ).iterator()).toArray(dummyArray));

        mapper.getSubtypeResolver()
        .registerSubtypes(Lists
                .newArrayList(Iterables.transform(ServiceLoader.load(ConnectorFactory.class),
                        new Function<ConnectorFactory,Class<ConnectorFactory>> () {
                            @SuppressWarnings("unchecked")
                            public Class<ConnectorFactory> apply(ConnectorFactory factory) {
                                return (Class<ConnectorFactory>) factory.getClass();
                            }
                        }
                    ).iterator()).toArray(dummyArray));

        // Issue-96: some types were not serializable
        final String json = mapper.writeValueAsString(configuration);
        assertThat(json)
                .isNotNull();

        // and as an added bonus, let's see we can also read it back:
        final Configuration cfg = mapper.readValue(json, HttpConfiguration.class);
        assertThat(cfg)
                .isNotNull();
    }
}
