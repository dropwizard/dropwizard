package io.dropwizard.hibernate;

import io.dropwizard.jersey.errors.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.sql.SQLNonTransientConnectionException;

@Provider
public class SQLNonTransientConnectionExceptionMapper implements ExceptionMapper<SQLNonTransientConnectionException> {
    @Override
    public Response toResponse(SQLNonTransientConnectionException e) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                       .entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "Connection not available"))
                       .build();

    }
}
