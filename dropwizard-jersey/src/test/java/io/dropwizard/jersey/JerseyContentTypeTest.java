package io.dropwizard.jersey;

import io.dropwizard.jersey.dummy.DummyResource;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class JerseyContentTypeTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(DummyResource.class);
    }

    @Test
    public void testValidContentType() {
        final Response response = target("/").request(MediaType.TEXT_PLAIN_TYPE).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("bar");
    }

    @Test
    public void testInvalidContentType() {
        final Response response = target("/").request("foo").get();

        assertThat(response.getStatus()).isEqualTo(406);
        assertThat(response.hasEntity()).isEqualTo(false);
    }
}
