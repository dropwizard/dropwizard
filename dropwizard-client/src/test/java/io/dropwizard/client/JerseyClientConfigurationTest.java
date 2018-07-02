package io.dropwizard.client;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.util.Resources;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class JerseyClientConfigurationTest {

    @Test
    public void testBasicJerseyClient() throws Exception {
        final JerseyClientConfiguration configuration = new YamlConfigurationFactory<>(JerseyClientConfiguration.class,
                Validators.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(new File(Resources.getResource("yaml/jersey-client.yml").toURI()));
        assertThat(configuration.getMinThreads()).isEqualTo(8);
        assertThat(configuration.getMaxThreads()).isEqualTo(64);
        assertThat(configuration.getWorkQueueSize()).isEqualTo(16);
        assertThat(configuration.isGzipEnabled()).isFalse();
        assertThat(configuration.isGzipEnabledForRequests()).isFalse();
        assertThat(configuration.isChunkedEncodingEnabled()).isFalse();
    }
}
