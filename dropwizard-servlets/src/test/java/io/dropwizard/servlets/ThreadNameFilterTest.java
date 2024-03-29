package io.dropwizard.servlets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThreadNameFilterTest {

    private HttpServletRequest request = mock(HttpServletRequest.class);

    private HttpServletResponse response = mock(HttpServletResponse.class);

    private FilterChain chain = mock(FilterChain.class);

    private FilterConfig filterConfig = mock(FilterConfig.class);

    private ThreadNameFilter threadNameFilter = new ThreadNameFilter();

    @BeforeEach
    void setUp() throws Exception {
        threadNameFilter.init(filterConfig);
    }

    @AfterEach
    void tearDown() throws Exception {
        threadNameFilter.destroy();
    }

    @Test
    void setsThreadNameInChain() throws Exception {
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
