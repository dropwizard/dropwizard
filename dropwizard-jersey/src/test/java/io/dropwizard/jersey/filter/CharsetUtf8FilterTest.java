package io.dropwizard.jersey.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CharsetUtf8FilterTest {

    private ContainerRequestContext request = mock(ContainerRequestContext.class);
    private ContainerResponseContext response = mock(ContainerResponseContext.class);

    private CharsetUtf8Filter charsetUtf8Filter = new CharsetUtf8Filter();

    @Test
    void testSetsCharsetEncoding() throws Exception {
        when(response.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE);
        when(response.getHeaders()).thenReturn(headers);

        charsetUtf8Filter.filter(request, response);

        assertThat((MediaType) headers.getFirst(HttpHeaders.CONTENT_TYPE))
            .isEqualTo(MediaType.valueOf("application/json;charset=UTF-8"));
    }
}
