package io.dropwizard.servlets.tasks;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskServletTest {
    private final Task gc = mock(Task.class);
    private final PostBodyTask printJSON = mock(PostBodyTask.class);

    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final TaskServlet servlet = new TaskServlet(metricRegistry);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    @BeforeEach
    void setUp() {
        when(gc.getName()).thenReturn("gc");
        when(printJSON.getName()).thenReturn("print-json");
        servlet.add(gc);
        servlet.add(printJSON);
    }

    @Test
    void returnsA404WhenNotFound() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/test");

        servlet.service(request, response);

        verify(response).setStatus(404);
    }

    @Test
    void runsATaskWhenFound() throws Exception {
        final PrintWriter output = mock(PrintWriter.class);
        final ServletInputStream bodyStream = new TestServletInputStream(
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(response.getWriter()).thenReturn(output);
        when(request.getInputStream()).thenReturn(bodyStream);

        servlet.service(request, response);

        verify(gc).execute(Collections.emptyMap(), output);
    }

    @Test
    void responseHasSpecifiedContentType() throws Exception {
        final ServletInputStream bodyStream = new TestServletInputStream(
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        when(request.getInputStream()).thenReturn(bodyStream);
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        when(gc.getResponseContentType()).thenReturn(Optional.of("application/json"));

        servlet.service(request, response);

        verify(response).setContentType("application/json");
    }

    @Test
    void responseHasDefaultContentTypeWhenNoneSpecified() throws Exception {
        final ServletInputStream bodyStream = new TestServletInputStream(
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        when(request.getInputStream()).thenReturn(bodyStream);
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        servlet.service(request, response);

        verify(response).setContentType("text/plain;charset=UTF-8");
    }

    @Test
    void passesQueryStringParamsAlong() throws Exception {
        final PrintWriter output = mock(PrintWriter.class);
        final ServletInputStream bodyStream = new TestServletInputStream(
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.singletonList("runs")));
        when(request.getParameterValues("runs")).thenReturn(new String[]{"1"});
        when(request.getInputStream()).thenReturn(bodyStream);
        when(response.getWriter()).thenReturn(output);

        servlet.service(request, response);

        verify(gc).execute(Collections.singletonMap("runs", Collections.singletonList("1")), output);
    }

    @Test
    void passesPostBodyAlongToPostBodyTasks() throws Exception {
        String body = "{\"json\": true}";
        final PrintWriter output = mock(PrintWriter.class);
        final ServletInputStream bodyStream = new TestServletInputStream(
            new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/print-json");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(request.getInputStream()).thenReturn(bodyStream);
        when(response.getWriter()).thenReturn(output);

        servlet.service(request, response);

        verify(printJSON).execute(Collections.emptyMap(), body, output);
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsA500OnExceptions() throws Exception {
        final ServletInputStream bodyStream = new TestServletInputStream(
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(request.getInputStream()).thenReturn(bodyStream);

        final PrintWriter output = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(output);

        final RuntimeException ex = new RuntimeException("whoops");

        doThrow(ex).when(gc).execute(any(Map.class), any(PrintWriter.class));

        servlet.service(request, response);

        verify(response).setStatus(500);
    }

    /**
     * Add a test to make sure the signature of the Task class does not change as the TaskServlet
     * depends on this to perform record metrics on Tasks
     */
    @Test
    void verifyTaskExecuteMethod() throws NoSuchMethodException {
        assertThat(Task.class
                .getMethod("execute", Map.class, PrintWriter.class)
                .getReturnType())
            .isEqualTo(Void.TYPE);
    }

    @Test
    void verifyPostBodyTaskExecuteMethod() throws NoSuchMethodException {
        assertThat(PostBodyTask.class
                .getMethod("execute", Map.class, String.class, PrintWriter.class)
                .getReturnType())
            .isEqualTo(Void.TYPE);
    }

    @Test
    void returnAllTaskNamesLexicallyOnGet() throws Exception {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            when(request.getMethod()).thenReturn("GET");
            when(request.getPathInfo()).thenReturn(null);
            when(response.getWriter()).thenReturn(pw);
            servlet.service(request, response);

            final String newLine = System.lineSeparator();
            assertThat(sw)
                .hasToString(gc.getName() + newLine + printJSON.getName() + newLine);
        }
    }

    @Test
    void returnsA404WhenGettingUnknownTask() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/absent");
        servlet.service(request, response);

        verify(response).setStatus(404);
    }

    @Test
    void returnsA405WhenGettingTaskByName() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/gc");
        servlet.service(request, response);

        verify(response).setStatus(405);
    }

    @Test
    void testRunsTimedTask() throws Exception {
        final ServletInputStream bodyStream = new TestServletInputStream(
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        final Task timedTask = new Task("timed-task") {
            @Override
            @Timed(name = "vacuum-cleaning")
            public void execute(Map<String, List<String>> parameters, PrintWriter output) {
                output.println("Vacuum cleaning");
            }
        };
        servlet.add(timedTask);

        when(request.getInputStream()).thenReturn(bodyStream);
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/timed-task");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        servlet.service(request, response);

        assertThat(metricRegistry.getTimers()).containsKey(name(timedTask.getClass(), "vacuum-cleaning"));
    }

    @Test
    void testRunsMeteredTask() throws Exception {
        final ServletInputStream bodyStream = new TestServletInputStream(
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        final Task meteredTask = new Task("metered-task") {
            @Override
            @Metered(name = "vacuum-cleaning")
            public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
                output.println("Vacuum cleaning");
            }
        };
        servlet.add(meteredTask);

        when(request.getMethod()).thenReturn("POST");
        when(request.getInputStream()).thenReturn(bodyStream);
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getPathInfo()).thenReturn("/metered-task");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        servlet.service(request, response);

        assertThat(metricRegistry.getMeters()).containsKey(name(meteredTask.getClass(), "vacuum-cleaning"));
    }

    @Test
    void testRunsExceptionMeteredTask() throws Exception {
        final ServletInputStream bodyStream = new TestServletInputStream(
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        final Task exceptionMeteredTask = new Task("exception-metered-task") {
            @Override
            @ExceptionMetered(name = "vacuum-cleaning-exceptions")
            public void execute(Map<String, List<String>> parameters, PrintWriter output) {
                throw new RuntimeException("The engine has died");
            }
        };
        servlet.add(exceptionMeteredTask);

        when(request.getInputStream()).thenReturn(bodyStream);
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/exception-metered-task");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        servlet.service(request, response);

        assertThat(metricRegistry.getMeters()).containsKey(name(exceptionMeteredTask.getClass(),
            "vacuum-cleaning-exceptions"));
    }

    @Test
    void testReturnsA404ForTaskRoot() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn(null);

        servlet.service(request, response);

        verify(response).setStatus(404);
    }

    @Test
    void testPrintStackTrackWhenEnabled() throws Exception {
        final TaskConfiguration taskConfiguration = new TaskConfiguration();
        taskConfiguration.setPrintStackTraceOnError(true);
        final TaskServlet servlet = new TaskServlet(metricRegistry, taskConfiguration);
        servlet.add(gc);
        final ServletInputStream bodyStream = new TestServletInputStream(
                new ByteArrayInputStream(new byte[0]));

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getInputStream()).thenReturn(bodyStream);

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter output = new PrintWriter(stringWriter, true);
        when(response.getWriter()).thenReturn(output);

        doThrow(new RuntimeException("whoops")).when(gc).execute(any(), any());

        servlet.service(request, response);

        assertThat(stringWriter.toString()).contains("java.lang.RuntimeException: whoops");
    }

    @Test
    void testDoNotPrintStackTrackWhenDisabled() throws Exception {
        final TaskConfiguration taskConfiguration = new TaskConfiguration();
        taskConfiguration.setPrintStackTraceOnError(false);
        final TaskServlet servlet = new TaskServlet(metricRegistry, taskConfiguration);
        servlet.add(gc);
        final ServletInputStream bodyStream = new TestServletInputStream(
                new ByteArrayInputStream(new byte[0]));

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/gc");
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getInputStream()).thenReturn(bodyStream);

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter output = new PrintWriter(stringWriter, true);
        when(response.getWriter()).thenReturn(output);

        doThrow(new RuntimeException("whoops")).when(gc).execute(any(), any());

        servlet.service(request, response);

        assertThat(stringWriter.toString().trim()).isEqualTo("whoops");
    }

    @SuppressWarnings("InputStreamSlowMultibyteRead")
    private static class TestServletInputStream extends ServletInputStream {
        private final InputStream delegate;

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
