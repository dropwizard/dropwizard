package io.dropwizard.testing.app;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link io.dropwizard.testing.junit.ResourceTestRule} with a different
 * test container factory.
 */
public class ResourceTestRuleWithGrizzlyTest {
    @Rule
    public final ResourceTestRule resourceTestRule = ResourceTestRule.builder()
            .addResource(ContextInjectionResource::new)
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(RuntimeExceptionMapper::new)
            .build();

    @Test
    public void testResource() {
        assertThat(resourceTestRule.target("test").request()
                .get(String.class))
                .isEqualTo("test");
    }

    @Test
    public void testExceptionMapper() {
        final Response resp = resourceTestRule.target("test").request()
                .post(Entity.json(""));
        assertThat(resp.getStatus()).isEqualTo(500);
        assertThat(resp.readEntity(String.class)).isEqualTo("Can't touch this");
    }

    @Test
    public void testClientSupportsPatchMethod() {
        final String resp = resourceTestRule.target("test")
            .request()
            .method("PATCH", Entity.text("Patch is working"), String.class);
        assertThat(resp).isEqualTo("Patch is working");
    }

    private static class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException exception) {
            return Response.serverError()
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .entity(exception.getMessage())
                    .build();
        }
    }
}
