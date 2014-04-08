package io.dropwizard.servlets.tasks;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class TaskServletTest {
    private final Task gc = mock(Task.class);
    private final Task clearCache = mock(Task.class);

    {
        when(gc.getName()).thenReturn("gc");
        when(clearCache.getName()).thenReturn("clear-cache");
    }

    private final TaskServlet servlet = new TaskServlet(new MetricRegistry());
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    @Before
    public void setUp() throws Exception {
        servlet.add(gc);
        servlet.add(clearCache);
    }

    @Test
    public void returnsA404WhenNotFound() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/test");

        servlet.service(request, response);

        verify(response).sendError(404);
    }

    @Test
    public void runsATaskWhenFound() throws Exception {
        final PrintWriter output = mock(PrintWriter.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(ImmutableList.<String>of()));
        when(response.getWriter()).thenReturn(output);

        servlet.service(request, response);

        verify(gc).execute(ImmutableMultimap.<String, String>of(), output);
    }

    @Test
    public void passesQueryStringParamsAlong() throws Exception {
        final PrintWriter output = mock(PrintWriter.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(ImmutableList.of("runs")));
        when(request.getParameterValues("runs")).thenReturn(new String[]{ "1" });
        when(response.getWriter()).thenReturn(output);

        servlet.service(request, response);

        verify(gc).execute(ImmutableMultimap.of("runs", "1"), output);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void returnsA500OnExceptions() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(ImmutableList.<String>of()));

        final PrintWriter output = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(output);

        final RuntimeException ex = new RuntimeException("whoops");

        doThrow(ex).when(gc).execute(any(ImmutableMultimap.class), any(PrintWriter.class));

        servlet.service(request, response);

        verify(response).setStatus(500);
    }

    /**
     * Add a test to make sure the signature of the Task class does not change as the TaskServlet
     * depends on this to perform record metrics on Tasks
     */
    @Test
    public void verifyTaskExecuteMethod() {
        try {
            Task.class.getMethod("execute", ImmutableMultimap.class, PrintWriter.class);
        } catch (NoSuchMethodException e) {
            Assert.fail("Execute method for " + Task.class.getName() + " not found");
        }
    }
}
