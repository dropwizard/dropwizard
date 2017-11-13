package io.dropwizard.servlets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ThreadNameFilterTest {

    private HttpServletRequest request = mock(HttpServletRequest.class);

    private HttpServletResponse response = mock(HttpServletResponse.class);

    private FilterChain chain = mock(FilterChain.class);

    private FilterConfig filterConfig = mock(FilterConfig.class);

    private ThreadNameFilter threadNameFilter = new ThreadNameFilter();

    @Before
    public void setUp() throws Exception {
        threadNameFilter.init(filterConfig);
    }

    @After
    public void tearDown() throws Exception {
        threadNameFilter.destroy();
    }

    @Test
    public void setsThreadNameInChain() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/some/path");

        doAnswer(invocationOnMock -> {
            assertThat(Thread.currentThread().getName()).isEqualTo("test-thread - GET /some/path");
            return null;
        }).when(chain).doFilter(request, response);

        Thread.currentThread().setName("test-thread");
        threadNameFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(Thread.currentThread().getName()).isEqualTo("test-thread");
    }

}
