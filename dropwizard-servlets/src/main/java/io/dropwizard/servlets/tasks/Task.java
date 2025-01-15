package io.dropwizard.servlets.tasks;

import org.jspecify.annotations.Nullable;

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
    @Nullable
    private final String responseContentType;

    /**
     * Create a new task with the given name.
     *
     * @param name the task's name
     */
    protected Task(String name) {
        this(name, null);
    }

    /**
     * Create a new task with the given name and response content type
     *
     * @param name                the task's name
     * @param responseContentType the task's response content type
     * @since 2.0
     */
    protected Task(String name, @Nullable String responseContentType) {
        this.name = name;
        this.responseContentType = responseContentType;
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
     * Returns the task's response content type.
     *
     * @return the task's response content type
     * @since 2.0
     */
    public Optional<String> getResponseContentType() {
        return Optional.ofNullable(responseContentType);
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
