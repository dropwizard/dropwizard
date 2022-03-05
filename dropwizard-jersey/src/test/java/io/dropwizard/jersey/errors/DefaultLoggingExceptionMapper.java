package io.dropwizard.jersey.errors;

import jakarta.ws.rs.ext.Provider;

@Provider
public class DefaultLoggingExceptionMapper extends LoggingExceptionMapper<Throwable> {
}
