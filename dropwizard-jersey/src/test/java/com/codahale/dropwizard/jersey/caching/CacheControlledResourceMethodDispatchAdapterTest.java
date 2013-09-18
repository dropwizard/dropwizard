package com.codahale.dropwizard.jersey.caching;

import com.codahale.dropwizard.logging.LoggingFactory;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;

import static org.fest.assertions.api.Assertions.assertThat;

public class CacheControlledResourceMethodDispatchAdapterTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder("com.codahale.dropwizard.jersey.caching").build();
    }

    @Test
    public void immutableResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = resource().path("/caching/immutable").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, max-age=31536000");
    }

    @Test
    public void privateResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = resource().path("/caching/private").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("private, no-transform");
    }

    @Test
    public void maxAgeResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = resource().path("/caching/max-age").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, max-age=1123200");
    }

    @Test
    public void noCacheResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = resource().path("/caching/no-cache").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-cache, no-transform");
    }

    @Test
    public void noStoreResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = resource().path("/caching/no-store").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-store, no-transform");
    }

    @Test
    public void noTransformResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = resource().path("/caching/no-transform").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .isNull();
    }

    @Test
    public void mustRevalidateResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = resource().path("/caching/must-revalidate").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, must-revalidate");
    }

    @Test
    public void proxyRevalidateResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = resource().path("/caching/proxy-revalidate").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, proxy-revalidate");
    }

    @Test
    public void sharedMaxAgeResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = resource().path("/caching/shared-max-age").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, s-maxage=46800");
    }
}
