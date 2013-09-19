package io.dropwizard.servlets;

import org.junit.Test;
import org.mockito.InOrder;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class CacheBustingFilterTest {
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final CacheBustingFilter filter = new CacheBustingFilter();

    @Test
    public void passesThroughNonHttpRequests() throws Exception {
        final ServletRequest req = mock(ServletRequest.class);
        final ServletResponse res = mock(ServletResponse.class);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyZeroInteractions(res);
    }

    @Test
    public void setsACacheHeaderOnTheResponse() throws Exception {
        filter.doFilter(request, response, chain);

        final InOrder inOrder = inOrder(response, chain);
        inOrder.verify(response).setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        inOrder.verify(chain).doFilter(request, response);
    }
}
