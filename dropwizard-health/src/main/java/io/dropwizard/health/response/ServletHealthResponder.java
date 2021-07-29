package io.dropwizard.health.response;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.PrintWriter;

import static java.util.Objects.requireNonNull;

public class ServletHealthResponder extends HttpServlet implements HealthResponder {
    public static final String CHECK_TYPE_QUERY_PARAM = "type";
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
        final HealthResponse healthResponse = healthResponseProvider()
            .currentHealthResponse(type);

        response.setContentType(healthResponse.getContentType());

        final PrintWriter writer = response.getWriter();
        writer.write(healthResponse.getMessage());
        if (!healthResponse.isHealthy()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public HealthResponseProvider healthResponseProvider() {
        return healthResponseProvider;
    }
}
