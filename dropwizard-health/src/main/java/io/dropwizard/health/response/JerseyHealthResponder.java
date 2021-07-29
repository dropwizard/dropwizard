package io.dropwizard.health.response;

import org.glassfish.jersey.process.Inflector;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;

public class JerseyHealthResponder implements HealthResponder, Inflector<ContainerRequestContext, Response> {
    public static final String CHECK_TYPE_QUERY_PARAM = "type";
    private final HealthResponseProvider healthResponseProvider;
    private final boolean cacheControlEnabled;
    private final String cacheControlValue;

    public JerseyHealthResponder(final HealthResponseProvider healthResponseProvider,
                                 final boolean cacheControlEnabled, final String cacheControlValue) {
        this.healthResponseProvider = requireNonNull(healthResponseProvider);
        this.cacheControlEnabled = cacheControlEnabled;
        this.cacheControlValue = requireNonNull(cacheControlValue);
    }

    @Override
    public HealthResponseProvider healthResponseProvider() {
        return healthResponseProvider;
    }

    @Override
    public Response apply(final ContainerRequestContext containerRequestContext) {
        final String type = containerRequestContext.getUriInfo()
            .getPathParameters()
            .getFirst(CHECK_TYPE_QUERY_PARAM);
        final Response.ResponseBuilder responseBuilder;
        final HealthResponse healthResponse = healthResponseProvider.currentHealthResponse(type);
        if (healthResponse.isHealthy()) {
            responseBuilder = Response.ok();
        } else {
            responseBuilder = Response.status(Response.Status.SERVICE_UNAVAILABLE);
        }
        responseBuilder.entity(healthResponse.getMessage());
        if (cacheControlEnabled) {
            responseBuilder.cacheControl(CacheControl.valueOf(cacheControlValue));
        }

        responseBuilder.type(healthResponse.getContentType());

        return responseBuilder.build();
    }
}
