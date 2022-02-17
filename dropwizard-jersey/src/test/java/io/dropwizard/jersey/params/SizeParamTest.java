package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.util.Size;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SizeParamTest {

    @Test
    void parseSizeKilobytes() {
        final SizeParam param = new SizeParam("10kb");
        assertThat(param.get())
            .isEqualTo(Size.kilobytes(10));
    }

    @Test
    void badValueThrowsException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new SizeParam("10 kelvins", "degrees"))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getResponse().getEntity()).isEqualTo(
                new ErrorMessage(400, "degrees is not a valid size.")
            ));
    }
}
