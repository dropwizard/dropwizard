package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.util.Duration;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class DurationParamTest {

    @Test
    public void parseDurationSeconds() throws Exception {
        final DurationParam param = new DurationParam("10 seconds");
        assertThat(param.get())
            .isEqualTo(Duration.seconds(10));
    }

    @Test
    public void badValueThrowsException() throws Exception {
        try {
            new DurationParam("invalid", "param_name");
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus())
                .isEqualTo(400);

            ErrorMessage entity = (ErrorMessage) response.getEntity();
            assertThat(entity.getCode()).isEqualTo(400);
            assertThat(entity.getMessage())
                .isEqualTo("param_name is not a valid duration.");
        }
    }

}
