package io.dropwizard.logging.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents event logging attributes.
 */
public enum EventAttribute {

    @JsonProperty("level") LEVEL,
    @JsonProperty("threadName") THREAD_NAME,
    @JsonProperty("mdc") MDC,
    @JsonProperty("loggerName") LOGGER_NAME,
    @JsonProperty("message") MESSAGE,
    @JsonProperty("exception") EXCEPTION,
    @JsonProperty("contextName") CONTEXT_NAME,
    @JsonProperty("timestamp") TIMESTAMP;
}
