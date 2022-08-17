package io.dropwizard.hibernate;

import org.h2.jdbc.JdbcSQLNonTransientConnectionException;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Arrays;

/**
 * Mapper for {@link JdbcSQLNonTransientConnectionException} instances.
 * Outputs a status {@link javax.ws.rs.core.Response.Status#SERVICE_UNAVAILABLE} and the mapped stack trace elements of the exception.
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
