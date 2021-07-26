package io.dropwizard.health;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import static java.util.Objects.requireNonNull;

public class HealthCheckServlet extends HttpServlet {
    private static final String CHECK_TYPE_QUERY_PARAM = "type";
    private final HealthStatusChecker healthStatusChecker;
    private final boolean cacheControlEnabled;
    private final String cacheControlValue;
    private final String contentType;
    private final String healthyValue;
    private final String unhealthyValue;

    public HealthCheckServlet(final HealthStatusChecker healthStatusChecker,
                              final boolean cacheControlEnabled, final String cacheControlValue,
                              final String contentType, final String healthyValue, final String unhealthyValue) {
        this.healthStatusChecker = requireNonNull(healthStatusChecker);
        this.cacheControlEnabled = cacheControlEnabled;
        this.cacheControlValue = requireNonNull(cacheControlValue);
        this.contentType = requireNonNull(contentType);
        this.healthyValue = requireNonNull(healthyValue);
        this.unhealthyValue = requireNonNull(unhealthyValue);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (cacheControlEnabled) {
            resp.setHeader(HttpHeaders.CACHE_CONTROL, cacheControlValue);
        }
        resp.setContentType(contentType);

        final String typeValue = req.getParameter(CHECK_TYPE_QUERY_PARAM);

        final PrintWriter writer = resp.getWriter();
        if (healthStatusChecker.isHealthy(typeValue)) {
            writer.print(healthyValue);
        } else {
            writer.print(unhealthyValue);
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
