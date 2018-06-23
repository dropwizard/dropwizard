package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.Test;

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
    public void aNonIntegerThrowsAnException() {
        assertThatThrownBy(() -> new IntParam("foo"))
            .isInstanceOfSatisfying(WebApplicationException.class, e -> {
                assertThat(e.getResponse().getStatus()).isEqualTo(400);
                assertThat(e.getResponse().getEntity()).isEqualTo(
                    new ErrorMessage(400, "Parameter is not a number.")
                );
            });
    }
}
