package io.dropwizard.servlets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThreadNameFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private FilterConfig filterConfig;

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
