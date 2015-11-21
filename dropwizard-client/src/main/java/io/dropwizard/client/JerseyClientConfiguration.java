package io.dropwizard.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * The configuration class used by {@link JerseyClientBuilder}. Extends
 * {@link HttpClientConfiguration}.
 *
 * @see HttpClientConfiguration
 * @see <a href="http://dropwizard.io/0.9.1/docs/manual/configuration.html#jerseyclient">Jersey Client Configuration</a>
 */
public class JerseyClientConfiguration extends HttpClientConfiguration {
    @Min(1)
    @Max(16 * 1024)
    private int minThreads = 1;

    @Min(1)
    @Max(16 * 1024)
    private int maxThreads = 128;

    @Min(1)
    @Max(16 * 1024)
    private int workQueueSize = 8;

    private boolean gzipEnabled = true;

    private boolean gzipEnabledForRequests = true;

    private boolean chunkedEncodingEnabled = true;

    @JsonProperty
    public int getMinThreads() {
        return minThreads;
    }

    @JsonProperty
    public void setMinThreads(int minThreads) {
        this.minThreads = minThreads;
    }

    @JsonProperty
    public int getMaxThreads() {
        return maxThreads;
    }

    @JsonProperty
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @JsonProperty
    public boolean isGzipEnabled() {
        return gzipEnabled;
    }

    @JsonProperty
    public void setGzipEnabled(boolean enabled) {
        this.gzipEnabled = enabled;
    }

    @JsonProperty
    public boolean isGzipEnabledForRequests() {
        return gzipEnabledForRequests;
    }

    @JsonProperty
    public void setGzipEnabledForRequests(boolean enabled) {
        this.gzipEnabledForRequests = enabled;
    }

    @JsonProperty
    public boolean isChunkedEncodingEnabled() {
        return chunkedEncodingEnabled;
    }

    @JsonProperty
    public void setChunkedEncodingEnabled(final boolean chunkedEncodingEnabled) {
        this.chunkedEncodingEnabled = chunkedEncodingEnabled;
    }

    @JsonProperty
    public int getWorkQueueSize() {
        return workQueueSize;
    }

    @JsonProperty
    public void setWorkQueueSize(int workQueueSize) {
        this.workQueueSize = workQueueSize;
    }

    @JsonIgnore
    @ValidationMethod(message = ".minThreads must be less than or equal to maxThreads")
    public boolean isThreadPoolSizedCorrectly() {
        return minThreads <= maxThreads;
    }

    @JsonIgnore
    @ValidationMethod(message = ".gzipEnabledForRequests requires gzipEnabled set to true")
    public boolean isCompressionConfigurationValid() {
        return !gzipEnabledForRequests || gzipEnabled;
    }
}
