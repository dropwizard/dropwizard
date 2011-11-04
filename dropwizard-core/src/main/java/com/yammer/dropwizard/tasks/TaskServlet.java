package com.yammer.dropwizard.tasks;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

// TODO: 10/12/11 <coda> -- write tests for TaskServlet
// TODO: 10/12/11 <coda> -- write docs for TaskServlet

public class TaskServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServlet.class);
    private final ImmutableMap<String, Task> tasks;

    public TaskServlet(Iterable<Task> tasks) {
        final ImmutableMap.Builder<String, Task> builder = ImmutableMap.builder();
        for (Task task : tasks) {
            builder.put("/" + task.getName(), task);
        }
        this.tasks = builder.build();
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {
        final Task task = tasks.get(req.getRequestURI());
        if (task != null) {
            try {
                resp.setContentType(MediaType.TEXT_PLAIN);
                final PrintWriter output = resp.getWriter();
                task.execute(getParams(req), output);
                output.close();
            } catch (Exception e) {
                LOGGER.error("Error running " + task.getName(), e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private Map<String, List<String>> getParams(HttpServletRequest req) {
        final Map<String, List<String>> results = new HashMap<String, List<String>>();
        final Enumeration names = req.getParameterNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            final String[] values = req.getParameterValues(name);
            results.put(name, Arrays.asList(values));
        }
        return results;
    }
}
