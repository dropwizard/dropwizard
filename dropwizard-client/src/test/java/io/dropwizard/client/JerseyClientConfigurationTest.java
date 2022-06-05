package io.dropwizard.client;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DefaultObjectMapperFactory;
import io.dropwizard.jersey.validation.Validators;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JerseyClientConfigurationTest {

    @Test
    void testBasicJerseyClient() throws Exception {
        final JerseyClientConfiguration configuration = new YamlConfigurationFactory<>(JerseyClientConfiguration.class,
                Validators.newValidator(), new DefaultObjectMapperFactory().newObjectMapper(), "dw")
                .build(new ResourceConfigurationSourceProvider(), "yaml/jersey-client.yml");
        assertThat(configuration.getMinThreads()).isEqualTo(8);
        assertThat(configuration.getMaxThreads()).isEqualTo(64);
        assertThat(configuration.getWorkQueueSize()).isEqualTo(16);
        assertThat(configuration.isGzipEnabled()).isFalse();
        assertThat(configuration.isGzipEnabledForRequests()).isFalse();
        assertThat(configuration.isChunkedEncodingEnabled()).isFalse();
    }
}
