package io.dropwizard.server;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jetty.MutableServletContextHandler;
import org.eclipse.jetty.server.handler.ContextHandler.Context;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * A factory for configuring the administrative servlet (and its child servlets) for the environment.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>metricsEnabled</td>
 *         <td>true</td>
 *         <td>
 *             Determines whether metrics information will be available via the
 *             {@link com.codahale.metrics.servlets.AdminServlet AdminServlet} endpoint.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>metricsUri</td>
 *         <td>/metrics</td>
 *         <td>The relative URI for metrics retrieval.</td>
 *     </tr>
 *     <tr>
 *         <td>pingEnabled</td>
 *         <td>true</td>
 *         <td>
 *             Determines whether a {@code ping} liveness function will be available via the
 *             {@link com.codahale.metrics.servlets.AdminServlet AdminServlet} endpoint.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>pingUri</td>
 *         <td>/ping</td>
 *         <td>The relative URI for liveness response retrieval.</td>
 *     </tr>
 *     <tr>
 *         <td>threadsEnabled</td>
 *         <td>true</td>
 *         <td>
 *             Determines whether thread dump information will be available via the
 *             {@link com.codahale.metrics.servlets.AdminServlet AdminServlet} endpoint.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>threadsUri</td>
 *         <td>/threads</td>
 *         <td>The relative URI for thread dump retrieval.</td>
 *     </tr>
 *     <tr>
 *         <td>healthcheckEnabled</td>
 *         <td>true</td>
 *         <td>
 *             Determines whether health check information will be available via the
 *             {@link com.codahale.metrics.servlets.AdminServlet AdminServlet} endpoint.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>healthcheckUri</td>
 *         <td>/healthcheck</td>
 *         <td>The relative URI for health check retrieval.</td>
 *     </tr>
 *     <tr>
 *         <td>cpuProfileEnabled</td>
 *         <td>true</td>
 *         <td>
 *             Determines whether CPU profiling results will be available via the
 *             {@link com.codahale.metrics.servlets.AdminServlet AdminServlet} endpoint.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>cpuProfileUri</td>
 *         <td>/pprof</td>
 *         <td>The relative URI for CPU profiling retrieval.</td>
 *     </tr>
 * </table>
 *
 * @since 2.1
 */
public class AdminServletFactory {

    private boolean metricsEnabled = true;

    private String metricsUri = AdminServlet.DEFAULT_METRICS_URI;

    private boolean pingEnabled = true;

    private String pingUri = AdminServlet.DEFAULT_PING_URI;

    private boolean threadsEnabled = true;

    private String threadsUri = AdminServlet.DEFAULT_THREADS_URI;

    private boolean healthcheckEnabled = true;

    private String healthcheckUri = AdminServlet.DEFAULT_HEALTHCHECK_URI;

    private boolean cpuProfileEnabled = true;

    private String cpuProfileUri = AdminServlet.DEFAULT_CPU_PROFILE_URI;

    @JsonProperty("metricsEnabled")
    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    @JsonProperty("metricsEnabled")
    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    @JsonProperty("metricsUri")
    public String getMetricsUri() {
        return metricsUri;
    }

    @JsonProperty("metricsUri")
    public void setMetricsUri(String metricsUri) {
        this.metricsUri = metricsUri;
    }

    @JsonProperty("pingEnabled")
    public boolean isPingEnabled() {
        return pingEnabled;
    }

    @JsonProperty("pingEnabled")
    public void setPingEnabled(boolean pingEnabled) {
        this.pingEnabled = pingEnabled;
    }

    @JsonProperty("pingUri")
    public String getPingUri() {
        return pingUri;
    }

    @JsonProperty("pingUri")
    public void setPingUri(String pingUri) {
        this.pingUri = pingUri;
    }

    @JsonProperty("threadsEnabled")
    public boolean isThreadsEnabled() {
        return threadsEnabled;
    }

    @JsonProperty("threadsEnabled")
    public void setThreadsEnabled(boolean threadsEnabled) {
        this.threadsEnabled = threadsEnabled;
    }

    @JsonProperty("threadsUri")
    public String getThreadsUri() {
        return threadsUri;
    }

    @JsonProperty("threadsUri")
    public void setThreadsUri(String threadsUri) {
        this.threadsUri = threadsUri;
    }

    @JsonProperty("healthcheckEnabled")
    public boolean isHealthcheckEnabled() {
        return healthcheckEnabled;
    }

    @JsonProperty("healthcheckEnabled")
    public void setHealthcheckEnabled(boolean healthcheckEnabled) {
        this.healthcheckEnabled = healthcheckEnabled;
    }

    @JsonProperty("healthcheckUri")
    public String getHealthcheckUri() {
        return healthcheckUri;
    }

    @JsonProperty("healthcheckUri")
    public void setHealthcheckUri(String healthcheckUri) {
        this.healthcheckUri = healthcheckUri;
    }

    @JsonProperty("cpuProfileEnabled")
    public boolean isCpuProfileEnabled() {
        return cpuProfileEnabled;
    }

    @JsonProperty("cpuProfileEnabled")
    public void setCpuProfileEnabled(boolean cpuProfileEnabled) {
        this.cpuProfileEnabled = cpuProfileEnabled;
    }

    @JsonProperty("cpuProfileUri")
    public String getCpuProfileUri() {
        return cpuProfileUri;
    }

    @JsonProperty("cpuProfileUri")
    public void setCpuProfileUri(String cpuProfileUri) {
        this.cpuProfileUri = cpuProfileUri;
    }

    public void addServlet(MutableServletContextHandler handler,
                           MetricRegistry metrics,
                           HealthCheckRegistry healthChecks) {
        final Context servletContext = handler.getServletContext();
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, metrics);
        servletContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthChecks);

        final ServletHolder adminHolder = handler.addServlet(AdminServlet.class, "/*");
        adminHolder.setInitParameter(AdminServlet.METRICS_ENABLED_PARAM_KEY, Boolean.toString(metricsEnabled));
        adminHolder.setInitParameter(AdminServlet.METRICS_URI_PARAM_KEY, metricsUri);
        adminHolder.setInitParameter(AdminServlet.PING_ENABLED_PARAM_KEY, Boolean.toString(pingEnabled));
        adminHolder.setInitParameter(AdminServlet.PING_URI_PARAM_KEY, pingUri);
        adminHolder.setInitParameter(AdminServlet.THREADS_ENABLED_PARAM_KEY, Boolean.toString(threadsEnabled));
        adminHolder.setInitParameter(AdminServlet.THREADS_URI_PARAM_KEY, threadsUri);
        adminHolder.setInitParameter(AdminServlet.HEALTHCHECK_ENABLED_PARAM_KEY,
            Boolean.toString(healthcheckEnabled));
        adminHolder.setInitParameter(AdminServlet.HEALTHCHECK_URI_PARAM_KEY, healthcheckUri);
        adminHolder.setInitParameter(AdminServlet.CPU_PROFILE_ENABLED_PARAM_KEY,
            Boolean.toString(cpuProfileEnabled));
        adminHolder.setInitParameter(AdminServlet.CPU_PROFILE_URI_PARAM_KEY, cpuProfileUri);
    }
}
