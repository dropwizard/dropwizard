package io.dropwizard.jersey.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuntimeFilterTest {

    private ContainerRequestContext request = mock(ContainerRequestContext.class);
    private ContainerResponseContext response = mock(ContainerResponseContext.class);

    private RuntimeFilter runtimeFilter = new RuntimeFilter();

    @Test
    void testSetsCurrentTimeProperty() throws Exception {
        runtimeFilter.setCurrentTimeProvider(() -> 1510330745000000L);
        runtimeFilter.filter(request);
        Mockito.verify(request).setProperty("io.dropwizard.jersey.filter.runtime", 1510330745000000L);
    }

    @Test
    void testAddsXRuntimeHeader() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(response.getHeaders()).thenReturn(headers);
        when(request.getProperty("io.dropwizard.jersey.filter.runtime")).thenReturn(1510330745000000L);

        runtimeFilter.setCurrentTimeProvider(() -> 1510330868000000L);
        runtimeFilter.filter(request, response);

        assertThat(headers.getFirst("X-Runtime")).isEqualTo("0.123000");
    }
}
