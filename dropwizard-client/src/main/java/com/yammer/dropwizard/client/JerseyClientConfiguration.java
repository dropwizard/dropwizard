package com.yammer.dropwizard.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * The configuration class used by {@link JerseyClientBuilder}. Extends
 * {@link HttpClientConfiguration}.
 *
 * <h1>Additional Properties</h1>
 * <table>
 *     <tr>
 *         <td>Property Name</td>
 *         <td>Required</td>
 *         <td>Description</td>
 *         <td>Default Value</td>
 *     </tr>
 *     <tr>
 *         <td>{@code minThreads}</td>
 *         <td>No</td>
 *         <td>
 *             The minimum number of threads to use for asynchronous calls.
 *         </td>
 *         <td>1</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxThreads}</td>
 *         <td>No</td>
 *         <td>
 *             The maximum number of threads to use for asynchronous calls.
 *         </td>
 *         <td>128</td>
 *     </tr>
 *     <tr>
 *         <td>{@code gzipEnabled}</td>
 *         <td>No</td>
 *         <td>
 *             If {@code true}, the {@link com.sun.jersey.api.client.Client} will decode response
 *             entities with {@code gzip} content encoding.
 *         </td>
 *         <td>{@code true}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code gzipEnabledForRequests}</td>
 *         <td>No</td>
 *         <td>
 *             If {@code true}, the {@link com.sun.jersey.api.client.Client} will encode request
 *             entities with {@code gzip} content encoding. (Requires {@code gzipEnabled} to be
 *             {@code true}.
 *         </td>
 *         <td>{@code true}</td>
 *     </tr>
 * </table>
 *
 * @see HttpClientConfiguration
 */
public class JerseyClientConfiguration extends HttpClientConfiguration {
    @Min(1)
    @Max(16 * 1024)
    @JsonProperty
    private int minThreads = 1;

    @Min(1)
    @Max(16 * 1024)
    @JsonProperty
    private int maxThreads = 128;

    @JsonProperty
    private boolean gzipEnabled = true;

    @JsonProperty
    private boolean gzipEnabledForRequests = true;

    public int getMinThreads() {
        return minThreads;
    }

    public void setMinThreads(int minThreads) {
        this.minThreads = minThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public boolean isGzipEnabled() {
        return gzipEnabled;
    }

    public void setGzipEnabled(boolean enabled) {
        this.gzipEnabled = enabled;
    }

    public boolean isGzipEnabledForRequests() {
        return gzipEnabledForRequests;
    }

    public void setGzipEnabledForRequests(boolean enabled) {
        this.gzipEnabledForRequests = enabled;
    }

    @ValidationMethod(message = ".minThreads must be less than or equal to maxThreads")
    public boolean isThreadPoolSizedCorrectly() {
        return minThreads <= maxThreads;
    }

    @ValidationMethod(message = ".gzipEnabledForRequests requires gzipEnabled set to true")
    public boolean isCompressionConfigurationValid() {
        return !gzipEnabledForRequests || gzipEnabled;
    }
}
