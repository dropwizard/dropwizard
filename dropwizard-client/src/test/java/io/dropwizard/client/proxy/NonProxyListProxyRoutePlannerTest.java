package io.dropwizard.client.proxy;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NonProxyListProxyRoutePlannerTest {

    private HttpHost proxy = new HttpHost("192.168.52.15");
    private NonProxyListProxyRoutePlanner routePlanner = new NonProxyListProxyRoutePlanner(proxy,
            Arrays.asList("localhost", "*.example.com", "192.168.52.*"));
    private HttpRequest httpRequest = mock(HttpRequest.class);
    private HttpContext httpContext = mock(HttpContext.class);

    @Test
    void testProxyListIsNotSet() {
        assertThat(new NonProxyListProxyRoutePlanner(proxy, null).getNonProxyHostPatterns()).isEmpty();
    }

    @Test
    void testHostNotInBlackList() throws Exception {
        assertThat(routePlanner.determineProxy(new HttpHost("dropwizard.io"), httpRequest, httpContext))
                .isEqualTo(proxy);
    }

    @Test
    void testPlainHostIsMatched() throws Exception {
        assertThat(routePlanner.determineProxy(new HttpHost("localhost"), httpRequest, httpContext)).isNull();
    }

    @Test
    void testHostWithStartWildcardIsMatched() throws Exception {
        assertThat(routePlanner.determineProxy(new HttpHost("test.example.com"), httpRequest, httpContext)).isNull();
    }

    @Test
    void testHostWithEndWildcardIsMatched() throws Exception {
        assertThat(routePlanner.determineProxy(new HttpHost("192.168.52.94"), httpRequest, httpContext)).isNull();
    }
}
