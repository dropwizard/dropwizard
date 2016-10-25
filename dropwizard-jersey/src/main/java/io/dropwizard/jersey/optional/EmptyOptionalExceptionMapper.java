package io.dropwizard.jersey.optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * The default response when an empty optional is returned, is to respond with
 * a 404 NOT FOUND response.
 */
public class EmptyOptionalExceptionMapper implements ExceptionMapper<EmptyOptionalException> {
    @Override
    public Response toResponse(EmptyOptionalException exception) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
