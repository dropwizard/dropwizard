package io.dropwizard.jersey.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InstantParamTest {
    @Test
    public void parsesDateTimes() throws Exception {
        final InstantParam param = new InstantParam("2012-11-19T00:00:00Z");
        Instant instant = LocalDateTime.of(2012, 11, 19, 0, 0)
            .toInstant(ZoneOffset.UTC);

        assertThat(param.get())
            .isEqualTo(instant);
    }

    @Test
    public void nullThrowsAnException() throws Exception {
        try {
            new InstantParam(null, "myDate");
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus())
                .isEqualTo(400);

            ErrorMessage entity = (ErrorMessage) response.getEntity();
            assertThat(entity.getCode()).isEqualTo(400);
            assertThat(entity.getMessage())
                .isEqualTo("myDate must be in a ISO-8601 format.");
        }
    }
}
