package io.dropwizard.servlets.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.StringJoiner;

/**
 * A factory for configuring the tasks sub-system for the environment.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>printStackTraceOnError</td>
 *         <td>{@code false}</td>
 *         <td>Print the full stack trace when the execution of a task failed.</td>
 *     </tr>
 * </table>
 *
 * @since 2.0
 */
public class TaskConfiguration {
    private boolean printStackTraceOnError = false;

    @JsonProperty("printStackTraceOnError")
    public boolean isPrintStackTraceOnError() {
        return printStackTraceOnError;
    }

    @JsonProperty("printStackTraceOnError")
    public void setPrintStackTraceOnError(boolean printStackTraceOnError) {
        this.printStackTraceOnError = printStackTraceOnError;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TaskConfiguration.class.getSimpleName() + "[", "]")
                .add("printStackTraceOnError=" + printStackTraceOnError)
                .toString();
    }
}
