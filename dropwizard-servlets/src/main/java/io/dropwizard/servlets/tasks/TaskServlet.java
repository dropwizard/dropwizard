package io.dropwizard.servlets.tasks;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.codahale.metrics.MetricRegistry.name;

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
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {
        final Task task = tasks.get(req.getPathInfo());
        if (task != null) {
            resp.setContentType(MediaType.PLAIN_TEXT_UTF_8.toString());
            final PrintWriter output = resp.getWriter();
            try {
                final TaskExecutor taskExecutor = taskExecutors.get(task);
                taskExecutor.executeTask(getParams(req), output);
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

        public void executeTask(ImmutableMultimap<String, String> params, PrintWriter output) throws Exception {
            try {
                task.execute(params, output);
            } catch (Exception e) {
                throw e;
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
        public void executeTask(ImmutableMultimap<String, String> params, PrintWriter output) throws Exception {
            final Timer.Context context = timer.time();
            try {
                underlying.executeTask(params, output);
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
        public void executeTask(ImmutableMultimap<String, String> params, PrintWriter output) throws Exception {
            meter.mark();
            underlying.executeTask(params, output);
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

        @Override
        public void executeTask(ImmutableMultimap<String, String> params, PrintWriter output) throws Exception {
            try {
                underlying.executeTask(params, output);
            } catch (Exception e) {
                if (exceptionMeter != null) {
                    if (exceptionClass.isAssignableFrom(e.getClass()) ||
                            (e.getCause() != null && exceptionClass.isAssignableFrom(e.getCause().getClass()))) {
                        exceptionMeter.mark();
                    }
                }

                throw e;
            }
        }
    }

}
