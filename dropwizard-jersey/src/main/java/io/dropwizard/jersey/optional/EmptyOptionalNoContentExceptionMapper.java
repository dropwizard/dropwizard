package io.dropwizard.jersey.optional;

import java.util.Optional;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Returns a 204 for {@link Optional#empty()}
 * {@link EmptyOptionalExceptionMapper} returns a 404 for {@link Optional#empty()}
 *
 * @since 2.0
 */
public class EmptyOptionalNoContentExceptionMapper implements ExceptionMapper<EmptyOptionalException> {
    @Override
    public Response toResponse(EmptyOptionalException exception) {
        return Response.noContent().build();
    }
}
