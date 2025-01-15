package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.ContextInjectionResource;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class ResourceExtensionWithGrizzlyTest {

    private final ResourceExtension resources = ResourceExtension.builder()
        .addResource(ContextInjectionResource::new)
        .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
        .setClientConfigurator(clientConfig -> clientConfig.register(DummyExceptionMapper.class))
        .build();

    @Test
    void testClientSupportsPatchMethod() {
        final String resp = resources.target("test")
            .request()
            .method("PATCH", Entity.text("Patch is working"), String.class);
        assertThat(resp).isEqualTo("Patch is working");
    }

    @Test
    void testCustomClientConfiguration() {
        assertThat(resources.client().getConfiguration().isRegistered(DummyExceptionMapper.class)).isTrue();
    }

    private static class DummyExceptionMapper implements ExceptionMapper<WebApplicationException> {
        @Override
        public Response toResponse(WebApplicationException e) {
            throw new UnsupportedOperationException();
        }
    }
}
