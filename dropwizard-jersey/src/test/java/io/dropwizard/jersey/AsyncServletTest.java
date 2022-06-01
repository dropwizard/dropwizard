package io.dropwizard.jersey;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.jersey.dummy.DummyResource;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class AsyncServletTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting().register(DummyResource.class);
    }

    @Test
    void testAsyncResponse() {
        final Response response =
                target("/async").request(MediaType.TEXT_PLAIN_TYPE).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("foobar");
    }
}
