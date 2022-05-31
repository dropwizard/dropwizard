package io.dropwizard.servlets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CacheBustingFilterTest {
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final CacheBustingFilter filter = new CacheBustingFilter();

    @Test
    void passesThroughNonHttpRequests() throws Exception {
        final ServletRequest req = mock(ServletRequest.class);
        final ServletResponse res = mock(ServletResponse.class);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoInteractions(res);
    }

    @Test
    void setsACacheHeaderOnTheResponse() throws Exception {
        filter.doFilter(request, response, chain);

        final InOrder inOrder = inOrder(response, chain);
        inOrder.verify(response).setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        inOrder.verify(chain).doFilter(request, response);
    }
}
