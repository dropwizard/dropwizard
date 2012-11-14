package com.yammer.dropwizard.jersey.caching.tests;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.fest.assertions.api.Assertions.assertThat;

public class CacheControlledResourceMethodDispatchAdapterTest extends JerseyTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class ExampleResource {
        @GET
        @Path("/immutable")
        @CacheControl(immutable = true)
        public String showImmutable() {
            return "immutable";
        }

        @GET
        @Path("/private")
        @CacheControl(isPrivate = true)
        public String showPrivate() {
            return "private";
        }

        @GET
        @Path("/max-age")
        @CacheControl(maxAge = 13, maxAgeUnit = TimeUnit.DAYS)
        public String showMaxAge() {
            return "max-age";
        }

        @GET
        @Path("/no-cache")
        @CacheControl(noCache = true)
        public String showNoCache() {
            return "no-cache";
        }

        @GET
        @Path("/no-store")
        @CacheControl(noStore = true)
        public String showNoStore() {
            return "no-store";
        }

        @GET
        @Path("/no-transform")
        @CacheControl(noTransform = false)
        public String showNoTransform() {
            return "no-transform";
        }

        @GET
        @Path("/must-revalidate")
        @CacheControl(mustRevalidate = true)
        public String showMustRevalidate() {
            return "must-revalidate";
        }

        @GET
        @Path("/proxy-revalidate")
        @CacheControl(proxyRevalidate = true)
        public String showProxyRevalidate() {
            return "proxy-revalidate";
        }

        @GET
        @Path("/shared-max-age")
        @CacheControl(sharedMaxAge = 13, sharedMaxAgeUnit = TimeUnit.HOURS)
        public String showSharedMaxAge() {
            return "shared-max-age";
        }
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected AppDescriptor configure() {
        final DropwizardResourceConfig config = new DropwizardResourceConfig(true);
        config.getSingletons().add(new ExampleResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void immutableResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = client().resource("/test/immutable").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, max-age=31536000");
    }

    @Test
    public void privateResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = client().resource("/test/private").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("private, no-transform");
    }

    @Test
    public void maxAgeResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = client().resource("/test/max-age").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, max-age=1123200");
    }

    @Test
    public void noCacheResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = client().resource("/test/no-cache").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-cache, no-transform");
    }

    @Test
    public void noStoreResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = client().resource("/test/no-store").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-store, no-transform");
    }

    @Test
    public void noTransformResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = client().resource("/test/no-transform").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .isNull();
    }

    @Test
    public void mustRevalidateResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = client().resource("/test/must-revalidate").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, must-revalidate");
    }

    @Test
    public void proxyRevalidateResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = client().resource("/test/proxy-revalidate").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, proxy-revalidate");
    }

    @Test
    public void sharedMaxAgeResponsesHaveCacheControlHeaders() throws Exception {
        final ClientResponse response = client().resource("/test/shared-max-age").get(ClientResponse.class);

        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL))
                .containsOnly("no-transform, s-maxage=46800");
    }
}
