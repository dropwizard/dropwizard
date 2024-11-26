package io.dropwizard.jersey.errors;

import jakarta.ws.rs.core.Response;
import org.eclipse.jetty.io.EofException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EarlyEofExceptionMapperTest {
    @Test
    void testToReponse() {
        assertThat(new EarlyEofExceptionMapper().toResponse(new EofException()).getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
