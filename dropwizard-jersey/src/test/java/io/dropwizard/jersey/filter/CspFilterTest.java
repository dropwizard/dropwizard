package io.dropwizard.jersey.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CspFilterTest {

    private final ContainerRequestContext request = mock(ContainerRequestContext.class);
    private final ContainerResponseContext response = mock(ContainerResponseContext.class);
    private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    private final Map<String, Object> requestProperties = new HashMap<>();

    @BeforeEach
    void setUp() {
        headers.clear();
        requestProperties.clear();
        when(response.getHeaders()).thenReturn(headers);

        Mockito.doAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            final Object val = invocation.getArgument(1);
            requestProperties.put(key, val);
            return null;
        }).when(request).setProperty(Mockito.anyString(), Mockito.any());

        when(request.getProperty(Mockito.anyString())).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            return requestProperties.get(key);
        });
    }

    @Test
    void injectsCspHeadersWithGeneratedNonce() throws Exception {
        final CspFilter filter = new CspFilter(
                "default-src 'self'; script-src 'nonce-$NONCE';",
                "default-src 'self'; report-uri /report;"
        );

        // Run request filter to generate nonce
        filter.filter(request);

        final String nonce = (String) request.getProperty("csp-nonce");
        assertThat(nonce).isNotEmpty();

        // Run response filter to inject headers
        filter.filter(request, response);

        // Verify headers are present and $NONCE was replaced
        assertThat(headers.getFirst("Content-Security-Policy"))
                .isEqualTo("default-src 'self'; script-src 'nonce-" + nonce + "';");
        assertThat(headers.getFirst("Content-Security-Policy-Report-Only"))
                .isEqualTo("default-src 'self'; report-uri /report;");
    }

    @Test
    void generatesUniqueNonces() throws Exception {
        final CspFilter filter = new CspFilter(null, null);

        filter.filter(request);
        final String nonce1 = (String) request.getProperty("csp-nonce");

        filter.filter(request);
        final String nonce2 = (String) request.getProperty("csp-nonce");

        assertThat(nonce1).isNotEmpty();
        assertThat(nonce2).isNotEmpty();
        assertThat(nonce1).isNotEqualTo(nonce2);
    }
}
