package io.dropwizard.jersey.errors;

import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

class IllegalStateExceptionMapperTest {

    private final IllegalStateExceptionMapper mapper = new IllegalStateExceptionMapper();

    @Test
    void delegatesToParentClass() {
        @SuppressWarnings("serial")
        final Response reponse = mapper.toResponse(new IllegalStateException(getClass().getName()) {
        });
        assertThat(reponse.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    void handlesFormParamContentTypeError() {
        final Response reponse = mapper
            .toResponse(new IllegalStateException(LocalizationMessages.FORM_PARAM_CONTENT_TYPE_ERROR()));
        assertThat(reponse.getStatusInfo()).isEqualTo(UNSUPPORTED_MEDIA_TYPE);
        assertThat(reponse.getEntity()).isInstanceOf(ErrorMessage.class);
        assertThat(((ErrorMessage) reponse.getEntity()).getMessage())
            .isEqualTo(new NotSupportedException().getLocalizedMessage());
    }
}
