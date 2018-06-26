package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.ContextInjectionResource;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class ResourceExtensionWithGrizzlyTest {

    private ResourceExtension resources = ResourceExtension.builder()
        .addResource(ContextInjectionResource::new)
        .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
        .setClientConfigurator(clientConfig -> clientConfig.register(DummyExceptionMapper.class))
        .build();

    @Test
    public void testClientSupportsPatchMethod() {
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
