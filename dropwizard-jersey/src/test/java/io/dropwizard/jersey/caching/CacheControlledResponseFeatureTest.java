package io.dropwizard.jersey.caching;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheControlledResponseFeatureTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        ResourceConfig rc = DropwizardResourceConfig.forTesting();
        rc = rc.register(CachingResource.class);
        return rc;
    }

    @Test
    public void immutableResponsesHaveCacheControlHeaders() throws Exception {
        final Response response = target("/caching/immutable").request().get();

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, max-age=31536000");
    }

    @Test
    public void privateResponsesHaveCacheControlHeaders() throws Exception {
        final Response response = target("/caching/private").request().get();

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("private, no-transform");
    }

    @Test
    public void maxAgeResponsesHaveCacheControlHeaders() throws Exception {
        final Response response = target("/caching/max-age").request().get();

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, max-age=1123200");
    }

    @Test
    public void noCacheResponsesHaveCacheControlHeaders() throws Exception {
        final Response response = target("/caching/no-cache").request().get();

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-cache, no-transform");
    }

    @Test
    public void noStoreResponsesHaveCacheControlHeaders() throws Exception {
        final Response response = target("/caching/no-store").request().get();

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-store, no-transform");
    }

    @Test
    public void noTransformResponsesHaveCacheControlHeaders() throws Exception {
        final Response response = target("/caching/no-transform").request().get();

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .isNull();
    }

    @Test
    public void mustRevalidateResponsesHaveCacheControlHeaders() throws Exception {
        final Response response = target("/caching/must-revalidate").request().get();

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, must-revalidate");
    }

    @Test
    public void proxyRevalidateResponsesHaveCacheControlHeaders() throws Exception {
        final Response response = target("/caching/proxy-revalidate").request().get();

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, proxy-revalidate");
    }

    @Test
    public void sharedMaxAgeResponsesHaveCacheControlHeaders() throws Exception {
        final Response response = target("/caching/shared-max-age").request().get();

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, s-maxage=46800");
    }
}
