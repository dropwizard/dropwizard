package io.dropwizard.jersey.caching;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.TimeUnit;

@Path("/caching/")
@Produces(MediaType.TEXT_PLAIN)
public class CachingResource {
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
