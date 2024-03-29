package io.dropwizard.jersey.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestIdFilterTest {

    private ContainerRequestContext request = mock(ContainerRequestContext.class);
    private ContainerResponseContext response = mock(ContainerResponseContext.class);
    private Logger logger = mock(Logger.class);

    private RequestIdFilter requestIdFilter = new RequestIdFilter();
    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        requestIdFilter.setLogger(logger);

        when(request.getMethod()).thenReturn("GET");
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/some/path");
        when(request.getUriInfo()).thenReturn(uriInfo);
        when(response.getStatus()).thenReturn(200);
        when(response.getLength()).thenReturn(2048);

        when(response.getHeaders()).thenReturn(headers);
    }

    @Test
    void addsRandomRequestIdHeader() throws Exception {

        requestIdFilter.filter(request, response);

        String requestId = (String) headers.getFirst("X-Request-Id");
        assertThat(requestId).isNotNull();
        assertThat(UUID.fromString(requestId)).isNotNull();
        verify(logger).trace("method={} path={} request_id={} status={} length={}",
            "GET", "/some/path", requestId, 200, 2048);
    }

    @Test
    void doesNotAddRandomRequestIdHeaderIfItExists() throws Exception {
        String existedRequestId = "e286b503-aa36-43fe-8312-95ee8773e348";
        headers.add("X-Request-Id", existedRequestId);
        when(request.getHeaderString("X-Request-Id")).thenReturn(existedRequestId);

        requestIdFilter.filter(request, response);

        String requestId = (String) headers.getFirst("X-Request-Id");
        assertThat(requestId).isEqualTo(existedRequestId);
        verify(logger).trace("method={} path={} request_id={} status={} length={}",
            "GET", "/some/path", requestId, 200, 2048);
    }

}
