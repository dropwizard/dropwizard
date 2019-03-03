package io.dropwizard.jersey.errors;

import org.eclipse.jetty.io.EofException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

public class EarlyEofExceptionMapperTest {

    private final EarlyEofExceptionMapper mapper = new EarlyEofExceptionMapper();

    @Test
    public void testToReponse() {
        final Response reponse = mapper.toResponse(new EofException());
        Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), reponse.getStatus());
    }
}
