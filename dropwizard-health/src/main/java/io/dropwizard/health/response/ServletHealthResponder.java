package io.dropwizard.health.response;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import static java.util.Objects.requireNonNull;

public class ServletHealthResponder extends HttpServlet {
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

        final Map<String, Collection<String>> queryParameters = request.getParameterMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Arrays.asList(entry.getValue())
                ));

        final HealthResponse healthResponse = healthResponseProvider.healthResponse(queryParameters);

        response.setContentType(healthResponse.getContentType());

        response.getWriter()
                .write(healthResponse.getMessage());
        if (!healthResponse.isHealthy()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
