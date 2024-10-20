package io.dropwizard.servlets.tasks;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Objects.requireNonNull;

/**
 * A servlet which provides access to administrative {@link Task}s. It only responds to {@code POST}
 * requests, since most {@link Task}s aren't side effect free, and passes along the query string
 * parameters of the request to the task as a multimap.
 *
 * @see Task
 */
public class TaskServlet extends HttpServlet {
    private static final long serialVersionUID = 7404713218661358124L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServlet.class);
    private static final String DEFAULT_CONTENT_TYPE = "text/plain;charset=UTF-8";
    private final ConcurrentMap<String, Task> tasks;
    private final ConcurrentMap<Task, TaskExecutor> taskExecutors;

    private final MetricRegistry metricRegistry;
    private final TaskConfiguration taskConfiguration;

    /**
     * Creates a new TaskServlet.
     */
    public TaskServlet(MetricRegistry metricRegistry) {
        this(metricRegistry, new TaskConfiguration());
    }

    /**
     * Creates a new TaskServlet.
     *
     * @since 2.0
     */
    public TaskServlet(MetricRegistry metricRegistry, TaskConfiguration taskConfiguration) {
        this.metricRegistry = metricRegistry;
        this.taskConfiguration = taskConfiguration;
        this.tasks = new ConcurrentHashMap<>();
        this.taskExecutors = new ConcurrentHashMap<>();
    }

    public void add(Task task) {
        tasks.put('/' + task.getName(), task);

        TaskExecutor taskExecutor = new TaskExecutor(task);
        try {
            final Method executeMethod = task.getClass().getMethod("execute",
                    Map.class, PrintWriter.class);

            if (executeMethod.isAnnotationPresent(Timed.class)) {
                final Timed annotation = executeMethod.getAnnotation(Timed.class);
                final String name = chooseName(annotation.name(),
                        annotation.absolute(),
                        task);
                taskExecutor = new TimedTask(taskExecutor, metricRegistry.timer(name));
            }

            if (executeMethod.isAnnotationPresent(Metered.class)) {
                final Metered annotation = executeMethod.getAnnotation(Metered.class);
                final String name = chooseName(annotation.name(),
                                        annotation.absolute(),
                                        task);
                taskExecutor = new MeteredTask(taskExecutor, metricRegistry.meter(name));
            }

            if (executeMethod.isAnnotationPresent(ExceptionMetered.class)) {
                final ExceptionMetered annotation = executeMethod.getAnnotation(ExceptionMetered.class);
                final String name = chooseName(annotation.name(),
                                        annotation.absolute(),
                                        task,
                                        ExceptionMetered.DEFAULT_NAME_SUFFIX);
                taskExecutor = new ExceptionMeteredTask(taskExecutor, metricRegistry.meter(name), annotation.cause());
            }
        } catch (NoSuchMethodException ignored) {
        }

        taskExecutors.put(task, taskExecutor);
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        if (Optional.ofNullable(req.getPathInfo()).filter(s -> !s.isEmpty()).isEmpty()) {
            try (final PrintWriter output = resp.getWriter()) {
                resp.setContentType(DEFAULT_CONTENT_TYPE);
                getTasks().stream()
                    .map(Task::getName)
                    .sorted()
                    .forEach(output::println);
            } catch (IOException ioException) {
                LOGGER.error("Failed to write response", ioException);
                if (!resp.isCommitted()) {
                    resp.reset();
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } else if (tasks.containsKey(req.getPathInfo())) {
            if (!resp.isCommitted()) {
                resp.reset();
                resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } else {
            if (!resp.isCommitted()) {
                resp.reset();
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {
        final String pathInfo = req.getPathInfo();
        final Task task = pathInfo != null ? tasks.get(pathInfo) : null;
        if (task != null) {
            resp.setContentType(task.getResponseContentType().orElse(DEFAULT_CONTENT_TYPE));
            PrintWriter output;
            try {
                output = resp.getWriter();
            } catch (IOException ioException) {
                LOGGER.error("Failed to write response", ioException);
                if (!resp.isCommitted()) {
                    resp.reset();
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                return;
            }
            try {
                final TaskExecutor taskExecutor = taskExecutors.get(task);
                requireNonNull(taskExecutor, "taskExecutor").executeTask(getParams(req), getBody(req), output);
            } catch (Exception e) {
                LOGGER.error("Error running {}", task.getName(), e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                output.println();
                output.println(e.getMessage());
                if (taskConfiguration.isPrintStackTraceOnError()) {
                    e.printStackTrace(output);
                }
            } finally {
                output.close();
            }
        } else {
            if (!resp.isCommitted()) {
                resp.reset();
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    private static Map<String, List<String>> getParams(HttpServletRequest req) {
        final Map<String, List<String>> results = new HashMap<>();

        final Enumeration<String> names = req.getParameterNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final List<String> values = Arrays.asList(req.getParameterValues(name));
            results.put(name, values);
        }
        return results;
    }

    private String getBody(HttpServletRequest req) throws IOException {
        try (InputStream in = req.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public Collection<Task> getTasks() {
        return tasks.values();
    }

    private String chooseName(String explicitName, boolean absolute, Task task, String... suffixes) {
        if (explicitName != null && !explicitName.isEmpty()) {
            if (absolute) {
                return explicitName;
            }
            return name(task.getClass(), explicitName);
        }

        return name(task.getClass(), suffixes);
    }

    private static class TaskExecutor {
        private final Task task;

        private TaskExecutor(Task task) {
            this.task = task;
        }

        public void executeTask(Map<String, List<String>> params, String body, PrintWriter output) throws Exception {
            if (task instanceof PostBodyTask) {
                PostBodyTask postBodyTask = (PostBodyTask) task;
                postBodyTask.execute(params, body, output);
            } else {
                task.execute(params, output);
            }
        }
    }

    private static class TimedTask extends TaskExecutor {
        private final TaskExecutor underlying;
        private final Timer timer;

        private TimedTask(TaskExecutor underlying, Timer timer) {
            super(underlying.task);
            this.underlying = underlying;
            this.timer = timer;
        }

        @Override
        public void executeTask(Map<String, List<String>> params, String body, PrintWriter output) throws Exception {
            final Timer.Context context = timer.time();
            try {
                underlying.executeTask(params, body, output);
            } finally {
                context.stop();
            }
        }
    }

    private static class MeteredTask extends TaskExecutor {
        private final TaskExecutor underlying;
        private final Meter meter;

        private MeteredTask(TaskExecutor underlying, Meter meter) {
            super(underlying.task);
            this.meter = meter;
            this.underlying = underlying;
        }

        @Override
        public void executeTask(Map<String, List<String>> params, String body, PrintWriter output) throws Exception {
            meter.mark();
            underlying.executeTask(params, body, output);
        }
    }

    private static class ExceptionMeteredTask extends TaskExecutor {
        private final TaskExecutor underlying;
        private final Meter exceptionMeter;
        private final Class<?> exceptionClass;

        private ExceptionMeteredTask(TaskExecutor underlying,
                                     Meter exceptionMeter, Class<? extends Throwable> exceptionClass) {
            super(underlying.task);
            this.underlying = underlying;
            this.exceptionMeter = exceptionMeter;
            this.exceptionClass = exceptionClass;
        }

        private boolean isReallyAssignableFrom(Exception e) {
            return exceptionClass.isAssignableFrom(e.getClass()) ||
                (e.getCause() != null && exceptionClass.isAssignableFrom(e.getCause().getClass()));
        }

        @Override
        public void executeTask(Map<String, List<String>> params, String body, PrintWriter output) throws Exception {
            try {
                underlying.executeTask(params, body, output);
            } catch (Exception e) {
                if (exceptionMeter != null && isReallyAssignableFrom(e)) {
                    exceptionMeter.mark();
                } else {
                    throw e;
                }
            }
        }
    }

}
