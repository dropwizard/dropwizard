package io.dropwizard.jersey.errors;

import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.junit.Test;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;

public class IllegalStateExceptionMapperTest {

    private final IllegalStateExceptionMapper mapper = new IllegalStateExceptionMapper();

    @Test
    public void delegatesToParentClass() {
        @SuppressWarnings("serial")
        final Response reponse = mapper.toResponse(new IllegalStateException(getClass().getName()) {});
        assertThat(reponse.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void handlesFormParamContentTypeError() {
        final Response reponse =
                mapper.toResponse(new IllegalStateException(LocalizationMessages.FORM_PARAM_CONTENT_TYPE_ERROR()));
        assertThat(reponse.getStatusInfo()).isEqualTo(UNSUPPORTED_MEDIA_TYPE);
        assertThat(reponse.getEntity()).isInstanceOf(ErrorMessage.class);
        assertThat(((ErrorMessage) reponse.getEntity()).getMessage())
                .isEqualTo(new NotSupportedException().getLocalizedMessage());
    }
}
