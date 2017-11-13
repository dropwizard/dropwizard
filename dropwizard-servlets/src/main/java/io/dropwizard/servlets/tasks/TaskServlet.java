package io.dropwizard.servlets.tasks;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.CharStreams;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Objects.requireNonNull;

/**
 * A servlet which provides access to administrative {@link Task}s. It only responds to {@code POST}
 * requests, since most {@link Task}s aren't side-effect free, and passes along the query string
 * parameters of the request to the task as a multimap.
 *
 * @see Task
 */
public class TaskServlet extends HttpServlet {
    private static final long serialVersionUID = 7404713218661358124L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServlet.class);
    private final ConcurrentMap<String, Task> tasks;
    private final ConcurrentMap<Task, TaskExecutor> taskExecutors;

    private final MetricRegistry metricRegistry;

    /**
     * Creates a new TaskServlet.
     */
    public TaskServlet(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.tasks = new ConcurrentHashMap<>();
        this.taskExecutors = new ConcurrentHashMap<>();
    }

    public void add(Task task) {
        tasks.put('/' + task.getName(), task);

        TaskExecutor taskExecutor = new TaskExecutor(task);
        try {
            final Method executeMethod = task.getClass().getMethod("execute",
                    ImmutableMultimap.class, PrintWriter.class);

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
        if (Strings.isNullOrEmpty(req.getPathInfo())) {
            try (final PrintWriter output = resp.getWriter()) {
                resp.setContentType(MediaType.PLAIN_TEXT_UTF_8.toString());
                getTasks().stream()
                    .map(Task::getName)
                    .sorted()
                    .forEach(output::println);
            }
        } else if (tasks.containsKey(req.getPathInfo())) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {
        final Task task = tasks.get(req.getPathInfo());
        if (task != null) {
            resp.setContentType(MediaType.PLAIN_TEXT_UTF_8.toString());
            final PrintWriter output = resp.getWriter();
            try {
                final TaskExecutor taskExecutor = taskExecutors.get(task);
                requireNonNull(taskExecutor).executeTask(getParams(req), getBody(req), output);
            } catch (Exception e) {
                LOGGER.error("Error running {}", task.getName(), e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                output.println();
                output.println(e.getMessage());
                e.printStackTrace(output);
            } finally {
                output.close();
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static ImmutableMultimap<String, String> getParams(HttpServletRequest req) {
        final ImmutableMultimap.Builder<String, String> results = ImmutableMultimap.builder();
        final Enumeration<String> names = req.getParameterNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final String[] values = req.getParameterValues(name);
            results.putAll(name, values);
        }
        return results.build();
    }

    private String getBody(HttpServletRequest req) throws IOException {
        return CharStreams.toString(new InputStreamReader(req.getInputStream(), Charsets.UTF_8));
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

        public void executeTask(ImmutableMultimap<String, String> params, String body, PrintWriter output) throws Exception {
            if (task instanceof PostBodyTask) {
                PostBodyTask postBodyTask = (PostBodyTask) task;
                postBodyTask.execute(params, body, output);
            } else {
                task.execute(params, output);
            }
        }
    }

    private static class TimedTask extends TaskExecutor {
        private TaskExecutor underlying;
        private final Timer timer;

        private TimedTask(TaskExecutor underlying, Timer timer) {
            super(underlying.task);
            this.underlying = underlying;
            this.timer = timer;
        }

        @Override
        public void executeTask(ImmutableMultimap<String, String> params, String body, PrintWriter output) throws Exception {
            final Timer.Context context = timer.time();
            try {
                underlying.executeTask(params, body, output);
            } finally {
                context.stop();
            }
        }
    }

    private static class MeteredTask extends TaskExecutor {
        private TaskExecutor underlying;
        private final Meter meter;

        private MeteredTask(TaskExecutor underlying, Meter meter) {
            super(underlying.task);
            this.meter = meter;
            this.underlying = underlying;
        }

        @Override
        public void executeTask(ImmutableMultimap<String, String> params, String body, PrintWriter output) throws Exception {
            meter.mark();
            underlying.executeTask(params, body, output);
        }
    }

    private static class ExceptionMeteredTask extends TaskExecutor {
        private TaskExecutor underlying;
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
        public void executeTask(ImmutableMultimap<String, String> params, String body, PrintWriter output) throws Exception {
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
