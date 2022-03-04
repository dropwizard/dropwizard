package com.example.forms;

import io.dropwizard.core.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.Duration;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FormsAppTest {
    public static final DropwizardAppExtension<Configuration> RULE =
        new DropwizardAppExtension<>(FormsApp.class, "app1/config.yml", new ResourceConfigurationSourceProvider());

    private final JerseyClientConfiguration config = new JerseyClientConfiguration();

    @BeforeEach
    void setUp() {
        config.setTimeout(Duration.seconds(2));
    }

    @Test
    void canSubmitFormAndReceiveResponse() throws IOException {
        config.setChunkedEncodingEnabled(false);

        final Client client = new JerseyClientBuilder(RULE.getEnvironment())
            .using(config)
            .build("test client 1");

        try (final FormDataMultiPart fdmp = new FormDataMultiPart()) {
            final MultiPart mp = fdmp.bodyPart(new FormDataBodyPart(
                FormDataContentDisposition.name("file").fileName("fileName").build(), "CONTENT"));

            final String url = String.format("http://localhost:%d/uploadFile", RULE.getLocalPort());
            final String response = client.target(url).register(MultiPartFeature.class).request()
                .post(Entity.entity(mp, mp.getMediaType()), String.class);
            assertThat(response).isEqualTo("fileName:\nCONTENT");
        }
    }

    /**
     * Test confirms that chunked encoding has to be disabled in order for
     * sending forms to work. Maybe someday this requirement will be relaxed and
     * this test can be updated for the new behavior. For more info, see issues
     * #1013 and #1094
     */
    @Test
    void failOnNoChunkedEncoding() throws IOException {
        final Client client = new JerseyClientBuilder(RULE.getEnvironment())
            .using(config)
            .build("test client 2");

        try (final FormDataMultiPart fdmp = new FormDataMultiPart()) {
            final MultiPart mp = fdmp.bodyPart(new FormDataBodyPart(
                FormDataContentDisposition.name("file").fileName("fileName").build(), "CONTENT"));

            final String url = String.format("http://localhost:%d/uploadFile", RULE.getLocalPort());
            final Response response = client.target(url).register(MultiPartFeature.class).request()
                .post(Entity.entity(mp, mp.getMediaType()));
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.readEntity(ErrorMessage.class))
                .isEqualTo(new ErrorMessage(400, "HTTP 400 Bad Request"));
        }
    }
}
