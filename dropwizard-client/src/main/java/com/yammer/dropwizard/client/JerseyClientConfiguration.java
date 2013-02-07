package com.yammer.dropwizard.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * The configuration class used by {@link JerseyClientBuilder}. Extends
 * {@link HttpClientConfiguration}.
 *
 * @see HttpClientConfiguration
 * @see <a href="http://dropwizard.codahale.com/manual/client.html#man-client-jersey-config">Jersey Client Configuration</a>
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
