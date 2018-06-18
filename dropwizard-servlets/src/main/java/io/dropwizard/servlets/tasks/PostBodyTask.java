package io.dropwizard.servlets.tasks;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * A task which can be performed via the admin interface and provides the post body of the request.
 *
 * @see Task
 * @see TaskServlet
 */
public abstract class PostBodyTask extends Task {
    /**
     * Create a new task with the given name.
     *
     * @param name the task's name
     */
    protected PostBodyTask(String name) {
        super(name);
    }

    /**
     * @param parameters the query string parameters
     * @param body       the plain text request body
     * @param output     a {@link PrintWriter} wrapping the output stream of the task
     * @throws Exception
     */
    public abstract void execute(Map<String, List<String>> parameters,
                                 String body,
                                 PrintWriter output) throws Exception;

    /**
     * Deprecated, use `execute(parameters, body, output)` or inherit from Task instead.
     *
     * @param parameters the query string parameters
     * @param output     a {@link PrintWriter} wrapping the output stream of the task
     * @throws Exception
     */
    @Override
    @Deprecated
    public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
        throw new UnsupportedOperationException("Use `execute(parameters, body, output)`");
    }
}
