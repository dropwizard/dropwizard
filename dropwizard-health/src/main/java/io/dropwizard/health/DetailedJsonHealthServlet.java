package io.dropwizard.health;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class DetailedJsonHealthServlet extends HttpServlet {
    private static final String CHECK_TYPE_QUERY_PARAM = "type";
    private final HealthStatusChecker healthStatusChecker;
    private final HealthStateAggregator healthStateAggregator;
    private final ObjectMapper mapper;
    private final boolean cacheControlEnabled;
    private final String cacheControlValue;

    public DetailedJsonHealthServlet(final HealthStatusChecker healthStatusChecker,
                                     final HealthStateAggregator healthStateAggregator,
                                     final ObjectMapper mapper,
                                     final boolean cacheControlEnabled, final String cacheControlValue) {
        this.healthStatusChecker = requireNonNull(healthStatusChecker);
        this.healthStateAggregator = requireNonNull(healthStateAggregator);
        this.mapper = requireNonNull(mapper);
        this.cacheControlEnabled = cacheControlEnabled;
        this.cacheControlValue = requireNonNull(cacheControlValue);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (cacheControlEnabled) {
            resp.setHeader(HttpHeaders.CACHE_CONTROL, cacheControlValue);
        }
        resp.setContentType(MediaType.APPLICATION_JSON);
        final Collection<HealthStateView> healthStateViews = healthStateAggregator.healthStateViews();
        final String responseBody = mapper.writeValueAsString(healthStateViews);

        final String typeValue = req.getParameter(CHECK_TYPE_QUERY_PARAM);

        final PrintWriter writer = resp.getWriter();
        writer.write(responseBody);
        if (!healthStatusChecker.isHealthy(typeValue)) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
