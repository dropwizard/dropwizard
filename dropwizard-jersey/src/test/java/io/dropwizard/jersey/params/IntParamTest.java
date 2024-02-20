package io.dropwizard.jersey.params;

import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class IntParamTest {
    @Test
    void anIntegerReturnsAnInteger() {
        assertThat(new IntParam("200").get()).isEqualTo(200);
    }

    @Test
    void nullThrowsAnException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new IntParam(null))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("Parameter is not a number."));
    }

    @Test
    void emptyStringThrowsAnException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new IntParam(""))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("Parameter is not a number."));
    }

    @Test
    void aNonIntegerThrowsAnException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new IntParam("foo"))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("Parameter is not a number."));
    }

    @Test
    void aNonIntegerThrowsAnExceptionWithCustomName() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new IntParam("foo", "customName"))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("customName is not a number."));
    }
}
