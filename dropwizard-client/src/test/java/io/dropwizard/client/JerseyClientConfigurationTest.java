package io.dropwizard.client;

import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class JerseyClientConfigurationTest {

    @Test
    public void testBasicJerseyClient() throws Exception {
        final JerseyClientConfiguration configuration = new ConfigurationFactory<>(JerseyClientConfiguration.class,
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
