package io.dropwizard.jersey.optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Returns a 204 for Optional.empty()
 * {@link EmptyOptionalExceptionMapper} returns a 404 for Optional.empty()
 */
public class EmptyOptionalNoContentExceptionMapper implements ExceptionMapper<EmptyOptionalException> {
    @Override
    public Response toResponse(EmptyOptionalException exception) {
        return Response.noContent().build();
    }
}
