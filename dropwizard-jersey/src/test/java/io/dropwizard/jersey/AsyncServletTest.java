package io.dropwizard.jersey;

import io.dropwizard.jersey.dummy.DummyResource;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncServletTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(DummyResource.class);
    }

    @Test
    void testAsyncResponse() {
        final Response response = target("/async").request(MediaType.TEXT_PLAIN_TYPE).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("foobar");
    }
}
