package io.dropwizard.hibernate;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.h2.jdbc.JdbcSQLNonTransientConnectionException;

import java.util.Arrays;

/**
 * Mapper for {@link JdbcSQLNonTransientConnectionException} instances.
 * Outputs a status {@link Response.Status#SERVICE_UNAVAILABLE} and the mapped stack trace elements of the exception.
 */
public class JdbcSQLNonTransientConnectionExceptionMapper implements ExceptionMapper<JdbcSQLNonTransientConnectionException> {
    @Override
    public Response toResponse(JdbcSQLNonTransientConnectionException throwables) {
        MultivaluedMap<String, String> mappedStackTraceElements = new MultivaluedHashMap<>();
        Arrays
            .stream(throwables.getStackTrace())
            .forEach(stackTraceElement -> mappedStackTraceElements.add(stackTraceElement.getClassName(), stackTraceElement.getMethodName()));
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(mappedStackTraceElements).build();
    }
}
