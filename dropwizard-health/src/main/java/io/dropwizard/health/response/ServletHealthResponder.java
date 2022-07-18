package io.dropwizard.health.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.dropwizard.health.HealthCheckType;

import static java.util.Objects.requireNonNull;

import static io.dropwizard.health.response.JsonHealthResponseProvider.CHECK_TYPE_QUERY_PARAM;

public class ServletHealthResponder extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletHealthResponder.class);

    private final HealthResponseProvider healthResponseProvider;
    private final boolean cacheControlEnabled;
    private final String cacheControlValue;
    private final List<String> livenessCheckUrls;

    public ServletHealthResponder(final HealthResponseProvider healthResponseProvider,
                                  final boolean cacheControlEnabled, final String cacheControlValue) {
        this(healthResponseProvider, cacheControlEnabled, cacheControlValue, Collections.emptyList());
    }

    public ServletHealthResponder(final HealthResponseProvider healthResponseProvider,
        final boolean cacheControlEnabled, final String cacheControlValue, List<String> livenessCheckUrls ) {
        this.healthResponseProvider = requireNonNull(healthResponseProvider);
        this.cacheControlEnabled = cacheControlEnabled;
        this.cacheControlValue = requireNonNull(cacheControlValue);
        this.livenessCheckUrls =  livenessCheckUrls;
    }

    private boolean isLivenessCheck(HttpServletRequest request) {
        return livenessCheckUrls.stream().anyMatch(request.getServletPath()::equals);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (cacheControlEnabled) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, cacheControlValue);
        }

        final Map<String, Collection<String>> queryParameters = request.getParameterMap()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> Arrays.asList(entry.getValue())
            ));
        if(isLivenessCheck(request)){
            queryParameters.put(CHECK_TYPE_QUERY_PARAM, Collections.singletonList(HealthCheckType.ALIVE.name()));
        }

        final HealthResponse healthResponse = healthResponseProvider.healthResponse(queryParameters);

        response.setContentType(healthResponse.getContentType());

        try {
            response.getWriter()
                .write(healthResponse.getMessage());
            response.setStatus(healthResponse.getStatus());
        } catch (IOException ioException) {
            LOGGER.error("Failed to write response", ioException);
            if (!response.isCommitted()) {
                response.reset();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
