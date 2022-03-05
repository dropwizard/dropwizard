package io.dropwizard.client;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

public class ConfiguredCloseableHttpClient {
    private final CloseableHttpClient closeableHttpClient;
    private final RequestConfig defaultRequestConfig;

    /* package */ ConfiguredCloseableHttpClient(CloseableHttpClient closeableHttpClient, RequestConfig defaultRequestConfig) {
        this.closeableHttpClient = closeableHttpClient;
        this.defaultRequestConfig = defaultRequestConfig;
    }

    public RequestConfig getDefaultRequestConfig() {
        return defaultRequestConfig;
    }

    public CloseableHttpClient getClient() {
        return closeableHttpClient;
    }
}
