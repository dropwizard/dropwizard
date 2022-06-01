package io.dropwizard.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConfiguredCloseableHttpClientTest {
    private ConfiguredCloseableHttpClient configuredClient;

    private CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
    private RequestConfig defaultRequestConfigMock = Mockito.mock(RequestConfig.class);

    @BeforeEach
    void setUp() {
        configuredClient = new ConfiguredCloseableHttpClient(closeableHttpClientMock, defaultRequestConfigMock);
    }

    @Test
    void getDefaultRequestConfig_returns_config_provided_at_construction() {
        assertThat(configuredClient.getDefaultRequestConfig()).isEqualTo(defaultRequestConfigMock);
    }

    @Test
    void getClient_returns_config_provided_at_construction() {
        assertThat(configuredClient.getClient()).isEqualTo(closeableHttpClientMock);
    }
}
