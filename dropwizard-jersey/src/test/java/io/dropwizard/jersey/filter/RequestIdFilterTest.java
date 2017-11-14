package io.dropwizard.jersey.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestIdFilterTest {

    private ContainerRequestContext request = mock(ContainerRequestContext.class);
    private ContainerResponseContext response = mock(ContainerResponseContext.class);
    private Logger logger = mock(Logger.class);

    private RequestIdFilter requestIdFilter = new RequestIdFilter();
    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    @Before
    public void setUp() throws Exception {
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
    public void addsRandomRequestIdHeader() throws Exception {

        requestIdFilter.filter(request, response);

        String requestId = (String) headers.getFirst("X-Request-Id");
        assertThat(requestId).isNotNull();
        assertThat(UUID.fromString(requestId)).isNotNull();
        verify(logger).trace("method={} path={} request_id={} status={} length={}",
            "GET", "/some/path", requestId, 200, 2048);
    }

    @Test
    public void doesNotAddRandomRequestIdHeaderIfItExists() throws Exception {
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
