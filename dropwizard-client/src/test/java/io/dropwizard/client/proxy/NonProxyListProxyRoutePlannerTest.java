package io.dropwizard.client.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

class NonProxyListProxyRoutePlannerTest {

    private HttpHost proxy = new HttpHost("192.168.52.15");
    private NonProxyListProxyRoutePlanner routePlanner =
            new NonProxyListProxyRoutePlanner(proxy, Arrays.asList("localhost", "*.example.com", "192.168.52.*"));
    private HttpContext httpContext = mock(HttpContext.class);

    @Test
    void testProxyListIsNotSet() {
        assertThat(new NonProxyListProxyRoutePlanner(proxy, null).getNonProxyHostPatterns())
                .isEmpty();
    }

    @Test
    void testHostNotInBlackList() throws Exception {
        assertThat(routePlanner.determineProxy(new HttpHost("dropwizard.io"), httpContext))
                .isEqualTo(proxy);
    }

    @Test
    void testPlainHostIsMatched() throws Exception {
        assertThat(routePlanner.determineProxy(new HttpHost("localhost"), httpContext))
                .isNull();
    }

    @Test
    void testHostWithStartWildcardIsMatched() throws Exception {
        assertThat(routePlanner.determineProxy(new HttpHost("test.example.com"), httpContext))
                .isNull();
    }

    @Test
    void testHostWithEndWildcardIsMatched() throws Exception {
        assertThat(routePlanner.determineProxy(new HttpHost("192.168.52.94"), httpContext))
                .isNull();
    }
}
