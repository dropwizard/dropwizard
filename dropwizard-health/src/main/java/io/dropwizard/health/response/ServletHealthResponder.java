package io.dropwizard.health.response;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class ServletHealthResponder extends HttpServlet implements HealthResponder {
    public static final String CHECK_TYPE_QUERY_PARAM = "type";
    public static final String NAME_QUERY_PARAM = "name";
    public static final String ALL_VALUE = "all";

    private final HealthResponseProvider healthResponseProvider;
    private final boolean cacheControlEnabled;
    private final String cacheControlValue;

    public ServletHealthResponder(final HealthResponseProvider healthResponseProvider,
                                  final boolean cacheControlEnabled, final String cacheControlValue) {
        this.healthResponseProvider = requireNonNull(healthResponseProvider);
        this.cacheControlEnabled = cacheControlEnabled;
        this.cacheControlValue = requireNonNull(cacheControlValue);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (cacheControlEnabled) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, cacheControlValue);
        }
        final String type = request.getParameter(CHECK_TYPE_QUERY_PARAM);
        final String[] nameParameters = request.getParameterValues(NAME_QUERY_PARAM);
        Set<String> names = null;
        if (nameParameters != null) {
            names = Arrays.stream(nameParameters)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        }

        final HealthResponse healthResponse;
        if (names == null) {
            healthResponse = healthResponseProvider()
                .minimalHealthResponse(type);
        } else if (names.contains(ALL_VALUE)) {
            healthResponse = healthResponseProvider()
                .fullHealthResponse(type);
        } else {
            healthResponse = healthResponseProvider()
                .partialHealthResponse(type, names);
        }

        response.setContentType(healthResponse.getContentType());

        final PrintWriter writer = response.getWriter();
        healthResponse.getMessage().ifPresent(writer::write);
        if (!healthResponse.isHealthy()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public HealthResponseProvider healthResponseProvider() {
        return healthResponseProvider;
    }
}
