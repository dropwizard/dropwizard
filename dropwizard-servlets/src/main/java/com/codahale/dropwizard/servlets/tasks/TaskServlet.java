package com.codahale.dropwizard.servlets.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentMap;

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

    /**
     * Creates a new TaskServlet.
     */
    public TaskServlet() {
        this.tasks = Maps.newConcurrentMap();
    }

    public void add(Task task) {
        tasks.put('/' + task.getName(), task);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {
        final Task task = tasks.get(req.getPathInfo());
        if (task != null) {
            resp.setContentType(MediaType.PLAIN_TEXT_UTF_8.toString());
            final PrintWriter output = resp.getWriter();
            try {
                task.execute(getParams(req), output);
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
}
