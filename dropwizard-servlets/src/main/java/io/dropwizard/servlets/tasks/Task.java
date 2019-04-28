package io.dropwizard.servlets.tasks;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An arbitrary administrative task which can be performed via the admin interface.
 *
 * @see TaskServlet
 */
public abstract class Task {
    private final String name;
    private final Optional<String> responseContentType;

    /**
     * Create a new task with the given name.
     *
     * @param name the task's name
     */
    protected Task(String name) {
        this.name = name;
        this.responseContentType = Optional.empty();
    }

    /**
     * Create a new task with the given name and response content type
     *
     * @param name the task's name
     * @param responseContentType the task's response content type
     */
    protected Task(String name, String responseContentType) {
        this.name = name;
        this.responseContentType = Optional.ofNullable(responseContentType);
    }

    /**
     * Returns the task's name,
     *
     * @return the task's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the task's response content type,
     *
     * @return the task's response content type
     */
    public Optional<String> getResponseContentType() {
        return responseContentType;
    }

    /**
     * Executes the task.
     *
     * @param parameters the query string parameters
     * @param output     a {@link PrintWriter} wrapping the output stream of the task
     * @throws Exception if something goes wrong
     */
    public abstract void execute(Map<String, List<String>> parameters,
                                 PrintWriter output) throws Exception;
}
