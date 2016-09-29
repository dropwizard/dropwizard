package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.util.Size;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class SizeParamTest {

    @Test
    public void parseSizeKilobytes() throws Exception {
        final SizeParam param = new SizeParam("10kb");
        assertThat(param.get())
            .isEqualTo(Size.kilobytes(10));
    }

    @Test
    public void badValueThrowsException() throws Exception {
        final Throwable exn = catchThrowable(() -> new SizeParam("10 kelvins", "degrees"));
        assertThat(exn).isInstanceOf(WebApplicationException.class);
        final Response response = ((WebApplicationException) exn).getResponse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat((ErrorMessage) response.getEntity())
            .isEqualTo(new ErrorMessage(400, "degrees is not a valid size."));
    }
}
