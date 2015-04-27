package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class LongParamTest {
    @Test
    public void aLongReturnsALong() throws Exception {
        final LongParam param = new LongParam("200");

        assertThat(param.get())
                .isEqualTo(200L);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void aNonIntegerThrowsAnException() throws Exception {
        try {
            new LongParam("foo");
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus())
                    .isEqualTo(400);

            ErrorMessage entity = (ErrorMessage) response.getEntity();
            assertThat(entity.getCode()).isEqualTo(400);
            assertThat(entity.getMessage())
                    .isEqualTo("Parameter is not a number.");
        }
    }
}
