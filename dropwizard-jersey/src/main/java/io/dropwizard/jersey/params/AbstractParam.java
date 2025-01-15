package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class from which to build Jersey parameter classes.
 *
 * @param <T> the type of value wrapped by the parameter
 */
public abstract class AbstractParam<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractParam.class);
    private final String parameterName;
    private final T value;

    /**
     * Given an input value from a client, creates a parameter wrapping its parsed value.
     *
     * @param input an input value from a client request, might be {@code null}
     */
    protected AbstractParam(@Nullable String input) {
        this(input, "Parameter");
    }

    /**
     * Given an input value from a client, creates a parameter wrapping its parsed value.
     *
     * @param input         an input value from a client request, might be {@code null}
     * @param parameterName name of the parameter with the provided value
     */
    protected AbstractParam(@Nullable String input, String parameterName) {
        this.parameterName = parameterName;
        try {
            this.value = parse(input);
        } catch (final Exception e) {
            final ErrorMessage message = generateErrorMessage(input, e);
            throw new WebApplicationException(message.getMessage(), message.getCode());
        }
    }

    /**
     * Generates an {@code ErrorMessage} to return to the client.
     *
     * @param input the raw input value
     * @param e     the exception thrown while parsing {@code input}
     * @return the {@code ErrorMessage} with the message and status to return to the client
     */
    protected ErrorMessage generateErrorMessage(@Nullable String input, Exception e) {
        // maintains backwards compatibility if deprecated error(String, Exception) method is overridden
        final Response response = error(input, e);
        if (response != null) {
            throw new WebApplicationException(response);
        }

        LOGGER.debug("Invalid input received: {}", input);
        String errorMessage = errorMessage(e);
        if (errorMessage.contains("%s")) {
            errorMessage = String.format(errorMessage, parameterName);
        }
        return new ErrorMessage(getErrorStatus().getStatusCode(), errorMessage);
    }

    /**
     * Deprecated - instead of throwing a {@link WebApplicationException} that contains a response with an entity,
     * AbstractParam now throws a {@link WebApplicationException} that contains a message and status code,
     * and can be mapped to a response using exception mappers. This method is kept for backwards compatibility with
     * user-defined AbstractParam implementations that implement custom error behavior by overriding this method.
     * <p>
     * Given a string representation which was unable to be parsed and the exception thrown, produce
     * a {@link Response} to be sent to the client.
     *
     * @param input the raw input value
     * @param e     the exception thrown while parsing {@code input}
     * @return the {@link Response} to be sent to the client, or {@code null} to allow AbstractParam to throw
     * a {@code WebApplicationException} that contains a message and status code.
     * @deprecated instead of returning a {@link Response} from this method, subclasses should override
     * {@link #errorMessage} and {@link #getErrorStatus} to allow a {@link WebApplicationException} to be thrown
     * which is mapped to a response using the exception mappers.
     */
    @Nullable
    @Deprecated
    protected Response error(@Nullable String input, Exception e) {
        return null;
    }

    /**
     * Deprecated - the media type should be set by the exception mapper instead. This method is no longer called
     * by AbstractParam.
     *
     * @return not used
     * @deprecated the media type should be set by the exception mapper instead
     */
    @Deprecated
    protected MediaType mediaType() {
        return MediaType.APPLICATION_JSON_TYPE;
    }

    /**
     * Given a string representation which was unable to be parsed and the exception thrown, produce
     * an error message to be sent to the client.
     *
     * @param e the exception thrown while parsing {@code input}
     * @return the error message to be sent the client
     */
    protected String errorMessage(Exception e) {
        return String.format("%s is invalid: %s", parameterName, e.getMessage());
    }

    /**
     * Given a string representation which was unable to be parsed, produce a {@link Status} for the
     * {@link Response} to be sent to the client.
     *
     * @return the HTTP {@link Status} of the error message
     */
    @SuppressWarnings("MethodMayBeStatic")
    protected Status getErrorStatus() {
        return Status.BAD_REQUEST;
    }

   /**
    * Given a string representation, parse it and return an instance of the parameter type.
    *
    * @param input the raw input
    * @return {@code input}, parsed as an instance of {@code T}
    * @throws Exception if there is an error parsing the input
    */
    protected abstract T parse(@Nullable String input) throws Exception;

    /**
     * Returns the underlying value.
     *
     * @return the underlying value
     */
    public T get() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final AbstractParam<?> that = (AbstractParam<?>) obj;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
