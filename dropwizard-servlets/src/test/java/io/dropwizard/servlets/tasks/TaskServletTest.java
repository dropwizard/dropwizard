package io.dropwizard.servlets.tasks;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaskServletTest {
    private final Task gc = mock(Task.class);
    private final PostBodyTask printJSON = mock(PostBodyTask.class);

    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final TaskServlet servlet = new TaskServlet(metricRegistry);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    @Before
    public void setUp() throws Exception {
        when(gc.getName()).thenReturn("gc");
        when(printJSON.getName()).thenReturn("print-json");
        servlet.add(gc);
        servlet.add(printJSON);
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
        final ServletInputStream bodyStream = new TestServletInputStream(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(ImmutableList.of()));
        when(response.getWriter()).thenReturn(output);
        when(request.getInputStream()).thenReturn(bodyStream);

        servlet.service(request, response);

        verify(gc).execute(ImmutableMultimap.of(), output);
    }

    @Test
    public void passesQueryStringParamsAlong() throws Exception {
        final PrintWriter output = mock(PrintWriter.class);
        final ServletInputStream bodyStream = new TestServletInputStream(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(ImmutableList.of("runs")));
        when(request.getParameterValues("runs")).thenReturn(new String[]{"1"});
        when(request.getInputStream()).thenReturn(bodyStream);
        when(response.getWriter()).thenReturn(output);

        servlet.service(request, response);

        verify(gc).execute(ImmutableMultimap.of("runs", "1"), output);
    }

    @Test
    public void passesPostBodyAlongToPostBodyTasks() throws Exception {
        String body = "{\"json\": true}";
        final PrintWriter output = mock(PrintWriter.class);
        final ServletInputStream bodyStream = new TestServletInputStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/print-json");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(ImmutableList.of()));
        when(request.getInputStream()).thenReturn(bodyStream);
        when(response.getWriter()).thenReturn(output);

        servlet.service(request, response);

        verify(printJSON).execute(ImmutableMultimap.of(), body, output);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void returnsA500OnExceptions() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(ImmutableList.of()));

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

    @Test
    public void verifyPostBodyTaskExecuteMethod() {
        try {
            PostBodyTask.class.getMethod("execute", ImmutableMultimap.class, String.class, PrintWriter.class);
        } catch (NoSuchMethodException e) {
            Assert.fail("Execute method for " + PostBodyTask.class.getName() + " not found");
        }
    }

    @Test
    public void returnAllTaskNamesLexicallyOnGet() throws Exception {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            when(request.getMethod()).thenReturn("GET");
            when(request.getPathInfo()).thenReturn(null);
            when(response.getWriter()).thenReturn(pw);
            servlet.service(request, response);

            final String newLine = System.lineSeparator();
            assertThat(sw.toString())
                .isEqualTo(gc.getName() + newLine + printJSON.getName() + newLine);
        }
    }

    @Test
    public void returnsA404WhenGettingUnknownTask() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/absent");
        servlet.service(request, response);

        verify(response).sendError(404);
    }

    @Test
    public void returnsA405WhenGettingTaskByName() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/gc");
        servlet.service(request, response);

        verify(response).sendError(405);
    }

    @Test
    public void testRunsTimedTask() throws Exception {
        final Task timedTask = new Task("timed-task") {
            @Override
            @Timed(name = "vacuum-cleaning")
            public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
                output.println("Vacuum cleaning");
            }
        };
        servlet.add(timedTask);

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/timed-task");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        servlet.service(request, response);

        assertThat(metricRegistry.getTimers()).containsKey(name(timedTask.getClass(), "vacuum-cleaning"));
    }

    @Test
    public void testRunsMeteredTask() throws Exception {
        final Task meteredTask = new Task("metered-task") {
            @Override
            @Metered(name = "vacuum-cleaning")
            public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
                output.println("Vacuum cleaning");
            }
        };
        servlet.add(meteredTask);

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/metered-task");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        servlet.service(request, response);

        assertThat(metricRegistry.getMeters()).containsKey(name(meteredTask.getClass(), "vacuum-cleaning"));
    }

    @Test
    public void testRunsExceptionMeteredTask() throws Exception {
        final Task exceptionMeteredTask = new Task("exception-metered-task") {
            @Override
            @ExceptionMetered(name = "vacuum-cleaning-exceptions")
            public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
                throw new RuntimeException("The engine has died");
            }
        };
        servlet.add(exceptionMeteredTask);

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/exception-metered-task");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        servlet.service(request, response);

        assertThat(metricRegistry.getMeters()).containsKey(name(exceptionMeteredTask.getClass(),
            "vacuum-cleaning-exceptions"));
    }

    private static class TestServletInputStream extends ServletInputStream {
        private InputStream delegate;

        public TestServletInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }
    }
}
