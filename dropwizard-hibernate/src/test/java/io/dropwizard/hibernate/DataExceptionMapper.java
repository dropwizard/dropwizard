package io.dropwizard.hibernate;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.hibernate.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DataExceptionMapper implements ExceptionMapper<DataException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataException.class);

    @Override
    public Response toResponse(DataException e) {
        LOGGER.error("Hibernate error", e);
        String message = e.getCause().getMessage().contains("EMAIL") ? "Wrong email" : "Wrong input";

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), message))
                .build();
    }
}
