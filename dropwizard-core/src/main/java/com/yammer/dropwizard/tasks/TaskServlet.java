package com.yammer.dropwizard.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

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
    private final ImmutableMap<String, Task> tasks;

    /**
     * Creates a new TaskServlet given the provided set of {@link Task} instances.
     *
     * @param tasks a series of tasks which the servlet will provide access to
     */
    public TaskServlet(Iterable<Task> tasks) {
        final ImmutableMap.Builder<String, Task> builder = ImmutableMap.builder();
        for (Task task : tasks) {
            builder.put('/' + task.getName(), task);
        }
        this.tasks = builder.build();
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {
        final Task task = tasks.get(req.getPathInfo());
        if (task != null) {
            try {
                resp.setContentType(MediaType.TEXT_PLAIN);
                final PrintWriter output = resp.getWriter();
                try {
                    task.execute(getParams(req), output);
                } finally {
                    output.close();
                }
            } catch (Exception e) {
                LOGGER.error("Error running {}", task.getName(), e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
}
