package io.dropwizard.servlets;

import io.dropwizard.util.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SlowRequestFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private Logger logger;

    private SlowRequestFilter slowRequestFilter = new SlowRequestFilter(Duration.milliseconds(500));

    @Before
    public void setUp() throws Exception {
        slowRequestFilter.init(filterConfig);
        slowRequestFilter.setLogger(logger);
        slowRequestFilter.setCurrentTimeProvider(() -> 1510330244000000L);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/some/path");
    }

    @After
    public void tearDown() throws Exception {
        slowRequestFilter.destroy();
    }

    @Test
    public void logsSlowRequests() throws Exception {
        doAnswer(invocationOnMock -> {
            slowRequestFilter.setCurrentTimeProvider(() -> 1510330745000000L);
            return null;
        }).when(chain).doFilter(request, response);

        slowRequestFilter.doFilter(request, response, chain);

        verify(logger).warn("Slow request: {} {} ({}ms)", "GET", "/some/path", 501L);
    }

    @Test
    public void doesNotLogFastRequests() throws Exception {
        doAnswer(invocationOnMock -> {
            slowRequestFilter.setCurrentTimeProvider(() -> 1510330743000000L);
            return null;
        }).when(chain).doFilter(request, response);

        slowRequestFilter.doFilter(request, response, chain);

        verify(logger, never()).warn("Slow request: {} {} ({}ms)", "GET", "/some/path", 499L);
    }

}
