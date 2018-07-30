package io.dropwizard.jersey;

import io.dropwizard.jersey.dummy.DummyResource;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncServletTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(DummyResource.class);
    }

    @Test
    public void testAsyncResponse() {
        final Response response = target("/async").request(MediaType.TEXT_PLAIN_TYPE).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("foobar");
    }
}
