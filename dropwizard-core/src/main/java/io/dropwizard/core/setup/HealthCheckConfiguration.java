package io.dropwizard.core.setup;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.StringJoiner;

/**
 * A factory for configuring the health check sub-system for the environment.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>servletEnabled</td>
 *         <td>true</td>
 *         <td>Whether to enable the admin health check servlet.</td>
 *     </tr>
 *     <tr>
 *         <td>minThreads</td>
 *         <td>1</td>
 *         <td>The minimum number of threads for executing health checks.</td>
 *     </tr>
 *     <tr>
 *         <td>maxThreads</td>
 *         <td>4</td>
 *         <td>The maximum number of threads for executing health checks.</td>
 *     </tr>
 *     <tr>
 *         <td>workQueueSize</td>
 *         <td>1</td>
 *         <td>The length of the work queue for health check executions.</td>
 *     </tr>
 * </table>
 *
 * @since 2.0
 */
public class HealthCheckConfiguration {
    private boolean servletEnabled = true;
    private int minThreads = 1;
    private int maxThreads = 4;
    private int workQueueSize = 1;

    @JsonProperty("servletEnabled")
    public boolean isServletEnabled() {
        return servletEnabled;
    }

    @JsonProperty("servletEnabled")
    public void setServletEnabled(boolean servletEnabled) {
        this.servletEnabled = servletEnabled;
    }

    @JsonProperty("minThreads")
    public int getMinThreads() {
        return minThreads;
    }

    @JsonProperty("minThreads")
    public void setMinThreads(int minThreads) {
        this.minThreads = minThreads;
    }

    @JsonProperty("maxThreads")
    public int getMaxThreads() {
        return maxThreads;
    }

    @JsonProperty("maxThreads")
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @JsonProperty("workQueueSize")
    public int getWorkQueueSize() {
        return workQueueSize;
    }

    @JsonProperty("workQueueSize")
    public void setWorkQueueSize(int workQueueSize) {
        this.workQueueSize = workQueueSize;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HealthCheckConfiguration.class.getSimpleName() + "[", "]")
                .add("servletEnabled= " + servletEnabled)
                .add("minThreads=" + minThreads)
                .add("maxThreads=" + maxThreads)
                .add("workQueueSize=" + workQueueSize)
                .toString();
    }
}
