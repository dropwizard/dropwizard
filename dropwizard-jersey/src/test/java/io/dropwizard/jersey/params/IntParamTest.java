package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IntParamTest {
    @Test
    public void anIntegerReturnsAnInteger() {
        final IntParam param = new IntParam("200");

        assertThat(param.get())
                .isEqualTo(200);
    }

    @Test
    public void nullThrowsAnException() {
        assertThatThrownBy(() -> new IntParam(null))
                .isInstanceOfSatisfying(WebApplicationException.class, e -> {
                    assertThat(e.getResponse().getStatus()).isEqualTo(400);
                    assertThat(e.getResponse().getEntity()).isEqualTo(
                            new ErrorMessage(400, "Parameter is not a number.")
                    );
                });
    }

    @Test
    public void emptyStringThrowsAnException() {
        assertThatThrownBy(() -> new IntParam(""))
                .isInstanceOfSatisfying(WebApplicationException.class, e -> {
                    assertThat(e.getResponse().getStatus()).isEqualTo(400);
                    assertThat(e.getResponse().getEntity()).isEqualTo(
                            new ErrorMessage(400, "Parameter is not a number.")
                    );
                });
    }

    @Test
    public void aNonIntegerThrowsAnException() {
        assertThatThrownBy(() -> new IntParam("foo"))
            .isInstanceOfSatisfying(WebApplicationException.class, e -> {
                assertThat(e.getResponse().getStatus()).isEqualTo(400);
                assertThat(e.getResponse().getEntity()).isEqualTo(
                    new ErrorMessage(400, "Parameter is not a number.")
                );
            });
    }

    @Test
    public void aNonIntegerThrowsAnExceptionWithCustomName() {
        assertThatThrownBy(() -> new IntParam("foo", "customName"))
            .isInstanceOfSatisfying(WebApplicationException.class, e -> {
                assertThat(e.getResponse().getStatus()).isEqualTo(400);
                assertThat(e.getResponse().getEntity()).isEqualTo(
                    new ErrorMessage(400, "customName is not a number.")
                );
            });
    }
}
