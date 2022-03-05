package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import jakarta.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class InstantParamTest {
    @Test
    void parsesDateTimes() {
        final InstantParam param = new InstantParam("2012-11-19T00:00:00Z");
        Instant instant = LocalDateTime.of(2012, 11, 19, 0, 0)
            .toInstant(ZoneOffset.UTC);

        assertThat(param.get())
            .isEqualTo(instant);
    }

    @Test
    void nullThrowsAnException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new InstantParam(null, "myDate"))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getResponse().getEntity()).isEqualTo(
                new ErrorMessage(400, "myDate must be in a ISO-8601 format.")
            ));
    }
}
