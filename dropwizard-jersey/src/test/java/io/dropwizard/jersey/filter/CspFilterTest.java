package io.dropwizard.jersey.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Base64;
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

    /**
     * Verifies that the nonce stored in the request context is exactly the same value
     * substituted into the Content-Security-Policy header. An off-by-one or copy-paste
     * bug could store one nonce but inject another, breaking CSP protection.
     */
    @Test
    void nonceInRequestContextExactlyMatchesNonceInCspHeader() throws Exception {
        final CspFilter filter = new CspFilter(
                "script-src 'nonce-$NONCE';",
                null
        );

        filter.filter(request);
        final String storedNonce = (String) request.getProperty("csp-nonce");
        assertThat(storedNonce).isNotEmpty();

        filter.filter(request, response);

        final String cspHeader = (String) headers.getFirst("Content-Security-Policy");
        assertThat(cspHeader).isEqualTo("script-src 'nonce-" + storedNonce + "';");
    }

    /**
     * Verifies that sequential requests (simulating thread reuse) each get an independent
     * nonce that is correctly reflected in their respective CSP headers.
     */
    @Test
    void sequentialRequestsGetIndependentNoncesReflectedInHeaders() throws Exception {
        final CspFilter filter = new CspFilter("script-src 'nonce-$NONCE';", null);

        // --- Request 1 ---
        final Map<String, Object> props1 = new HashMap<>();
        final MultivaluedMap<String, Object> headers1 = new MultivaluedHashMap<>();

        final ContainerRequestContext req1 = mock(ContainerRequestContext.class);
        final ContainerResponseContext res1 = mock(ContainerResponseContext.class);
        when(res1.getHeaders()).thenReturn(headers1);
        Mockito.doAnswer(i -> { props1.put(i.getArgument(0), i.getArgument(1)); return null; })
               .when(req1).setProperty(Mockito.anyString(), Mockito.any());
        when(req1.getProperty(Mockito.anyString())).thenAnswer(i -> props1.get(i.<String>getArgument(0)));

        // --- Request 2 ---
        final Map<String, Object> props2 = new HashMap<>();
        final MultivaluedMap<String, Object> headers2 = new MultivaluedHashMap<>();

        final ContainerRequestContext req2 = mock(ContainerRequestContext.class);
        final ContainerResponseContext res2 = mock(ContainerResponseContext.class);
        when(res2.getHeaders()).thenReturn(headers2);
        Mockito.doAnswer(i -> { props2.put(i.getArgument(0), i.getArgument(1)); return null; })
               .when(req2).setProperty(Mockito.anyString(), Mockito.any());
        when(req2.getProperty(Mockito.anyString())).thenAnswer(i -> props2.get(i.<String>getArgument(0)));

        filter.filter(req1);
        filter.filter(req1, res1);
        filter.filter(req2);
        filter.filter(req2, res2);

        final String nonce1 = (String) props1.get("csp-nonce");
        final String nonce2 = (String) props2.get("csp-nonce");
        final String csp1 = (String) headers1.getFirst("Content-Security-Policy");
        final String csp2 = (String) headers2.getFirst("Content-Security-Policy");

        // Each request's nonce is unique
        assertThat(nonce1).isNotEqualTo(nonce2);

        // Each CSP header exactly reflects its own request's nonce
        assertThat(csp1).isEqualTo("script-src 'nonce-" + nonce1 + "';");
        assertThat(csp2).isEqualTo("script-src 'nonce-" + nonce2 + "';");
    }

    /**
     * Verifies the nonce is 256-bit entropy encoded with URL-safe Base64 (no padding).
     * 32 bytes → ceil(32 * 4/3) = 43 URL-safe Base64 chars, no '=', no '+', no '/'.
     */
    @Test
    void nonceIs256BitUrlSafeBase64WithoutPadding() throws Exception {
        final CspFilter filter = new CspFilter(null, null);

        filter.filter(request);
        final String nonce = (String) request.getProperty("csp-nonce");

        // 32 bytes of Base64 URL-safe without padding = 43 characters
        assertThat(nonce).hasSize(43);

        // Must be decodable by the URL-safe decoder
        final byte[] decoded = Base64.getUrlDecoder().decode(nonce);
        assertThat(decoded).hasSize(32);

        // Must not contain standard Base64 characters that are unsafe in HTTP headers
        assertThat(nonce).doesNotContain("+", "/", "=");
    }
}
