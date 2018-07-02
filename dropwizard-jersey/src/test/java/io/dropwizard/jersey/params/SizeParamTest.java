package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.util.Size;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SizeParamTest {

    @Test
    public void parseSizeKilobytes() {
        final SizeParam param = new SizeParam("10kb");
        assertThat(param.get())
            .isEqualTo(Size.kilobytes(10));
    }

    @Test
    public void badValueThrowsException() {
        assertThatThrownBy(() -> new SizeParam("10 kelvins", "degrees"))
            .isInstanceOfSatisfying(WebApplicationException.class, e -> {
                assertThat(e.getResponse().getStatus()).isEqualTo(400);
                assertThat(e.getResponse().getEntity()).isEqualTo(
                    new ErrorMessage(400, "degrees is not a valid size.")
                );
            });
    }
}
