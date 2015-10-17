package io.dropwizard.client;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;

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
