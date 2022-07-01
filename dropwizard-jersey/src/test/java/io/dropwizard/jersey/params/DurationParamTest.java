package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DurationParamTest {

    @Test
    void parseDurationSeconds() {
        assertThat(new DurationParam("10 seconds").get())
            .isEqualTo(Duration.seconds(10));
    }

    @Test
    void badValueThrowsException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new DurationParam("invalid", "param_name"))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("param_name is not a valid duration."));
    }
}
