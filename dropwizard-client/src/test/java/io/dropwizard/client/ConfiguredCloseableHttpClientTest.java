package io.dropwizard.client;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConfiguredCloseableHttpClientTest {
    public ConfiguredCloseableHttpClient configuredClient;
    @Mock
    private CloseableHttpClient closeableHttpClientMock;
    @Mock
    private RequestConfig defaultRequestConfigMock;

    @Before
    public void setUp() {
        configuredClient = new ConfiguredCloseableHttpClient(closeableHttpClientMock, defaultRequestConfigMock);
    }

    @Test
    public void getDefaultRequestConfig_returns_config_provided_at_construction() {
        assertThat(configuredClient.getDefaultRequestConfig()).isEqualTo(defaultRequestConfigMock);
    }

    @Test
    public void getClient_returns_config_provided_at_construction() {
        assertThat(configuredClient.getClient()).isEqualTo(closeableHttpClientMock);
    }
}