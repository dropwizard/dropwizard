package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class IntParamTest {
    @Test
    public void anIntegerReturnsAnInteger() {
        assertThat(new IntParam("200").get()).isEqualTo(200);
    }

    @Test
    public void nullThrowsAnException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new IntParam(null))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getResponse().getEntity()).isEqualTo(
                new ErrorMessage(400, "Parameter is not a number.")
            ));
    }

    @Test
    public void emptyStringThrowsAnException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new IntParam(""))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getResponse().getEntity()).isEqualTo(
                new ErrorMessage(400, "Parameter is not a number.")
            ));
    }

    @Test
    public void aNonIntegerThrowsAnException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new IntParam("foo"))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getResponse().getEntity()).isEqualTo(
                new ErrorMessage(400, "Parameter is not a number.")
            ));
    }

    @Test
    public void aNonIntegerThrowsAnExceptionWithCustomName() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new IntParam("foo", "customName"))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getResponse().getEntity()).isEqualTo(
                new ErrorMessage(400, "customName is not a number.")
            ));
    }
}
