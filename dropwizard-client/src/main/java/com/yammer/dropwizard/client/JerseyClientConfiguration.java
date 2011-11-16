package com.yammer.dropwizard.client;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class JerseyClientConfiguration extends HttpClientConfiguration {
    // TODO: 11/16/11 <coda> -- validate minThreads <= maxThreads

    @Max(16 * 1024)
    @Min(1)
    private int minThreads;

    @Max(16 * 1024)
    @Min(1)
    private int maxThreads;

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
}
